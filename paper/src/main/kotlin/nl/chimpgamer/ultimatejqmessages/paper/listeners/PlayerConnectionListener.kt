package nl.chimpgamer.ultimatejqmessages.paper.listeners

import com.github.shynixn.mccoroutine.folia.MCCoroutine
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
import java.util.concurrent.TimeUnit

class PlayerConnectionListener(private val plugin: UltimateJQMessagesPlugin) : Listener {
    private val joinMessageCooldownKey = "JoinMessageCooldown"
    private val quitMessageCooldownKey = "QuitMessageCooldown"

    @EventHandler(priority = EventPriority.MONITOR)
    fun AsyncPlayerPreLoginEvent.onAsyncPlayerPreLogin() {
        if (loginResult !== AsyncPlayerPreLoginEvent.Result.ALLOWED) return
        plugin.usersHandler.loadUser(uniqueId, name)
    }

    @EventHandler
    fun PlayerJoinEvent.onPlayerJoin() {
        joinMessage(null)
        if (Cooldown.hasCooldown(player.uniqueId, joinMessageCooldownKey) && !player.hasPermission("ultimatejqmessages.cooldown.bypass")) return
        val user = plugin.usersHandler.getIfLoaded(player.uniqueId) ?: return
        var joinMessage = (user.customJoinMessage ?: user.joinMessage?.message) ?: return
        joinMessage = plugin.settingsConfig.prefix(JoinQuitMessageType.JOIN) + joinMessage
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
        quitMessage = plugin.settingsConfig.prefix(JoinQuitMessageType.QUIT) + quitMessage
        quitMessage(quitMessage.parse(getDisplayNamePlaceholder(player, JoinQuitMessageType.QUIT)))

        val quitMessagesCooldown = plugin.settingsConfig.quitMessagesCooldown
        if (quitMessagesCooldown > 0) {
            Cooldown(player.uniqueId, joinMessageCooldownKey, Duration.ofSeconds(quitMessagesCooldown)).start()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun PlayerJoinEvent.onPlayerJoinHighest() {
        if (joinMessage() == null || joinMessage() == Component.empty()) return
        plugin.server.onlinePlayers.filter { plugin.usersHandler.getIfLoaded(player.uniqueId)?.showJoinQuitMessages == true }.forEach {
            val joinMessageDelay = plugin.settingsConfig.delay(JoinQuitMessageType.JOIN)
            if (joinMessageDelay > 0)
                plugin.paperScheduler.runDelayed({ it.sendMessage(joinMessage()!!) }, joinMessageDelay, TimeUnit.SECONDS)
            else it.sendMessage(joinMessage()!!)
         }
        plugin.server.consoleSender.sendMessage(joinMessage()!!)
        joinMessage(null)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun PlayerQuitEvent.onPlayerQuitHighest() {
        if (quitMessage() == null || quitMessage() == Component.empty()) return
        plugin.server.onlinePlayers.filter { plugin.usersHandler.getIfLoaded(player.uniqueId)?.showJoinQuitMessages == true }.forEach {
            val joinMessageDelay = plugin.settingsConfig.delay(JoinQuitMessageType.QUIT)
            if (joinMessageDelay > 0)
                plugin.paperScheduler.runDelayed({ it.sendMessage(quitMessage()!!) }, joinMessageDelay, TimeUnit.SECONDS)
            else it.sendMessage(quitMessage()!!)
        }
        plugin.server.consoleSender.sendMessage(quitMessage()!!)
        quitMessage(null)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerQuitEvent.onPlayerQuitMonitor() {
        plugin.usersHandler.unloadUser(player.uniqueId)
    }
}