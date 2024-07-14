package nl.chimpgamer.ultimatejqmessages.paper.utils

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.util.EnumMap
import java.util.UUID
import kotlin.collections.HashSet

class PlayerChatInput<T>(
    private val plugin: Plugin,
    private val player: Player,
    private var value: T? = null,
    private val invalidInputMessage: Component?,
    private val sendValueMessage: Component?,
    private val isValidInput: (Player, String) -> Boolean,
    private val setValue: (Player, String) -> T?,
    private val onFinish: (Player, T) -> Unit,
    private val onCancel: (Player) -> Unit,
    private val cancel: String,
    private val onInvalidInput: (Player, String) -> Boolean,
    private val repeat: Boolean,
    private val chainAfter: EnumMap<EndReason, PlayerChatInput<*>>?,
    private val expiresAfter: Long,
    private val onExpire: (Player) -> Unit,
    private val onExpireMessage: Component?,
    private val onDisconnect: Runnable
) : Listener {
    private var started: Boolean = false
    private var job: Job? = null
    private var end: EndReason? = null

    @Suppress("DEPRECATION")
    @EventHandler
    fun AsyncPlayerChatEvent.onAsyncChat() {
        if (isCancelled) return
        if (this@PlayerChatInput.player != player) return
        if (!isStarted()) return
        isCancelled = true
        plugin.launch(plugin.entityDispatcher(player), CoroutineStart.UNDISPATCHED) { runEventOnMainThread(message) }
    }

    private fun runEventOnMainThread(message: String) {
        if (message.equals(cancel, ignoreCase = true)) {
            onCancel(player)
            end(EndReason.PLAYER_CANCELS)
            return
        }

        if (isValidInput(player, message)) {
            value = setValue(player, message)
            onFinish(player, value!!)
            end(EndReason.FINISH)
            return
        }

        if (onInvalidInput(player, message)) {
            if (invalidInputMessage != null) {
                player.sendMessage(invalidInputMessage)
            }
            if (sendValueMessage != null && repeat) {
                player.sendMessage(sendValueMessage)
            }
        }
        if (!repeat) {
            onExpire(player)
            end(EndReason.INVALID_INPUT)
        }
    }

    @EventHandler
    fun PlayerQuitEvent.onPlayerQuit() {
        if (this@PlayerChatInput.player != player) return
        if (!isStarted()) return
        onDisconnect.run()
        end(EndReason.PLAYER_DISCONNECTS)
    }

    @Throws(IllegalAccessError::class)
    fun start() {
        // The player can only be in one active PlayerChatInput at a time
        if (isInputting(player.uniqueId)) throw IllegalAccessError("Can't ask for input to a player that is already inputting")
        addPlayer(player.uniqueId)

        plugin.server.pluginManager.registerEvents(this, plugin)

        if (expiresAfter > 0) {
            plugin.launch(plugin.entityDispatcher(player), CoroutineStart.UNDISPATCHED) {
                delay(expiresAfter * 50L - 25)
                if (!isStarted()) return@launch
                onExpire(player)
                if (onExpireMessage != null) {
                    player.sendMessage(onExpireMessage)
                }
                end(EndReason.RUN_OUT_OF_TIME)
            }
        }
        if (sendValueMessage != null) {
            player.sendMessage(sendValueMessage)
        }
        started = true
        end = null
    }

    private fun unregister() {
        job?.cancel()
        // The player can be asked for an input again
        removePlayer(player.uniqueId)
        // Unregister events
        HandlerList.unregisterAll(this)
    }

    private fun end(reason: EndReason) {
        started = false
        end = reason

        unregister()
        if (chainAfter != null) {
            chainAfter[end]?.start()
        }
    }

    private fun isStarted(): Boolean = started

    data class PlayerChatInputBuilder<U : Any>(
        private val plugin: Plugin,
        private val player: Player,
    ) {

        private var invalidInputMessage: Component? = Component.text("That is not a valid input")
        private var sendValueMessage: Component? = Component.text("Send in the chat the value")
        private var onExpireMessage: Component? = Component.text("You ran out of time to answer")
        private var cancel: String = "cancel"

        private var onInvalidInput: (Player, String) -> Boolean = { _, _ -> true }
        private var isValidInput: (Player, String) -> Boolean = { _, _ -> true }
        private var setValue: (Player, String) -> U? = { _, _ -> value }
        private var onFinish: (Player, U) -> Unit = { _, _ -> }
        private var onCancel: (Player) -> Unit = { _ -> }
        private var onExpire: (Player) -> Unit = { _ -> }
        private var onDisconnect: Runnable = Runnable { }

        private var expiresAfter: Long = -1
        private var repeat: Boolean = true

        private var value: U? = null
        private var chainAfter: EnumMap<EndReason, PlayerChatInput<*>>? = null

        fun onInvalidInput(onInvalidInput: (Player, String) -> Boolean) = apply { this.onInvalidInput = onInvalidInput }
        fun isValidInput(isValidInput: (Player, String) -> Boolean) = apply { this.isValidInput = isValidInput }
        fun setValue(setValue: (Player, String) -> U) = apply { this.setValue = setValue }
        fun onFinish(onFinish: (Player, U) -> Unit) = apply { this.onFinish = onFinish }
        fun onCancel(onCancel: (Player) -> Unit) = apply { this.onCancel = onCancel }
        fun invalidInputMessage(invalidInputMessage: Component?) =
            apply { this.invalidInputMessage = invalidInputMessage }

        fun sendValueMessage(sendValueMessage: Component?) = apply { this.sendValueMessage = sendValueMessage }
        fun toCancel(cancel: String) = apply { this.cancel = cancel }
        fun defaultValue(defaultValue: U) = apply { this.value = defaultValue }
        fun repeat(repeat: Boolean) = apply { this.repeat = repeat }
        fun chainAfter(toChain: PlayerChatInput<*>, vararg after: EndReason) = apply {
            if (this.chainAfter == null) {
                chainAfter = EnumMap(EndReason::class.java)
            }
            for (endReason in after) {
                if (endReason === EndReason.PLAYER_DISCONNECTS) {
                    continue
                }
                this.chainAfter!![endReason] = toChain
            }
        }

        fun onExpire(onExpire: (Player) -> Unit) = apply { this.onExpire = onExpire }
        fun onExpireMessage(onExpireMessage: Component?) = apply { this.onExpireMessage = onExpireMessage }
        fun expiresAfter(expiresAfter: Long) = apply {
            if (expiresAfter > 0) {
                this.expiresAfter = expiresAfter
            }
        }

        fun onDisconnect(onDisconnect: Runnable) = apply { this.onDisconnect = onDisconnect }

        fun build() = PlayerChatInput(
            plugin,
            player,
            value,
            invalidInputMessage,
            sendValueMessage,
            isValidInput,
            setValue,
            onFinish,
            onCancel,
            cancel,
            onInvalidInput,
            repeat,
            chainAfter,
            expiresAfter,
            onExpire,
            onExpireMessage,
            onDisconnect
        )
    }

    companion object {
        private val players: MutableSet<UUID> = HashSet()

        private fun addPlayer(player: UUID) = players.add(player)

        private fun removePlayer(player: UUID) = players.remove(player)

        /**
         * Checks if a player is in an input-proces
         *
         * @param player
         * The UUID of the player to check if it is in an input-process
         *
         * @return True if the player is in an input process
         */
        fun isInputting(player: UUID): Boolean = players.contains(player)
    }

    enum class EndReason {
        /**
         * Used when the player sends as input the canceling string
         */
        PLAYER_CANCELS,

        /**
         * The input-process ended successfully
         */
        FINISH,

        /**
         * The player ran out of time to answer
         */
        RUN_OUT_OF_TIME,

        /**
         * The player disconnected
         */
        PLAYER_DISCONNECTS,

        /**
         * The player sent an invalid input and the repeating mode is off
         */
        INVALID_INPUT,

        /**
         * A plugin ended the input process
         */
        CUSTOM
    }
}