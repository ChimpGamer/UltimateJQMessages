package nl.chimpgamer.ultimatejqmessages.paper.listeners

import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.getDisplayNamePlaceholder
import nl.chimpgamer.ultimatejqmessages.paper.extensions.parse
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import nl.chimpgamer.ultimatejqmessages.paper.utils.Cooldown
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

class PlayerConnectionListener(private val plugin: UltimateJQMessagesPlugin) : Listener {
    private val joinMessageCooldownKey = "JoinMessageCooldown"
    private val quitMessageCooldownKey = "QuitMessageCooldown"

    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun AsyncPlayerPreLoginEvent.onAsyncPlayerPreLogin() {
        if (loginResult !== AsyncPlayerPreLoginEvent.Result.ALLOWED) return
        plugin.usersHandler.loadUser(uniqueId, name)
    }

    @EventHandler
    fun PlayerJoinEvent.onPlayerJoin() {
        joinMessage(null)
        if (Cooldown.hasCooldown(player.uniqueId, joinMessageCooldownKey) && !player.hasPermission("ultimatejqmessages.cooldown.bypass")) return
        val user = plugin.usersHandler.getIfLoaded(player.uniqueId) ?: return
        var joinMessage = (user.customJoinMessage ?: user.joinMessage?.message) ?: return
        joinMessage = plugin.settingsConfig.joinMessagesPrefix + joinMessage
        joinMessage(joinMessage.parse(getDisplayNamePlaceholder(player, JoinQuitMessageType.JOIN)))

        val joinMessagesCooldown = plugin.settingsConfig.joinMessagesCooldown
        if (joinMessagesCooldown > 0) {
            Cooldown(player.uniqueId, joinMessageCooldownKey, Duration.ofSeconds(joinMessagesCooldown)).start()
        }
    }

    @EventHandler
    fun PlayerQuitEvent.onPlayerQuit() {
        quitMessage(null)
        if (Cooldown.hasCooldown(player.uniqueId, quitMessageCooldownKey) && !player.hasPermission("ultimatejqmessages.cooldown.bypass")) return
        val user = plugin.usersHandler.getIfLoaded(player.uniqueId) ?: return
        var quitMessage = (user.customQuitMessage ?: user.quitMessage?.message) ?: return
        quitMessage = plugin.settingsConfig.quitMessagesPrefix + quitMessage
        quitMessage(quitMessage.parse(getDisplayNamePlaceholder(player, JoinQuitMessageType.QUIT)))

        val quitMessagesCooldown = plugin.settingsConfig.quitMessagesCooldown
        if (quitMessagesCooldown > 0) {
            Cooldown(player.uniqueId, joinMessageCooldownKey, Duration.ofSeconds(quitMessagesCooldown)).start()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun PlayerJoinEvent.onPlayerJoinHighest() {
        if (joinMessage() == null || joinMessage() == Component.empty()) return
        val joinMessage = joinMessage()!!
        joinMessage(null)
        val joinMessageDelay = plugin.settingsConfig.joinMessagesDelay
        if (joinMessageDelay > 0)
            delay(joinMessageDelay.seconds)
        plugin.server.onlinePlayers.filter { plugin.usersHandler.getIfLoaded(player.uniqueId)?.showJoinQuitMessages == true }.forEach {
            it.sendMessage(joinMessage)
         }
        plugin.server.consoleSender.sendMessage(joinMessage)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun PlayerQuitEvent.onPlayerQuitHighest() {
        if (quitMessage() == null || quitMessage() == Component.empty()) return
        val quitMessage = quitMessage()!!
        quitMessage(null)
        val quitMessageDelay = plugin.settingsConfig.quitMessagesDelay
        if (quitMessageDelay > 0)
            delay(quitMessageDelay.seconds)
        plugin.server.onlinePlayers.filter { plugin.usersHandler.getIfLoaded(player.uniqueId)?.showJoinQuitMessages == true }.forEach {
            it.sendMessage(quitMessage)
        }
        plugin.server.consoleSender.sendMessage(quitMessage)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerQuitEvent.onPlayerQuitMonitor() {
        plugin.usersHandler.unloadUser(player.uniqueId)
    }
}