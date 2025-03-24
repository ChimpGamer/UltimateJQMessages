package nl.chimpgamer.ultimatejqmessages.paper.hooks

import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import org.bukkit.entity.Player

class PlaceholderAPIHook(plugin: UltimateJQMessagesPlugin) : PluginHook(plugin, "PlaceholderAPI") {
    private lateinit var expansion: PapiPlaceholderExpansion

    override fun load() {
        if (canHook()) {
            expansion = PapiPlaceholderExpansion(plugin).also { it.register() }
            plugin.logger.info("Hooked into $pluginName")
        }
    }

    override fun unload() {
        if (this::expansion.isInitialized) {
            expansion.unregister()
        }
    }
}

class PapiPlaceholderExpansion(private val plugin: UltimateJQMessagesPlugin) : PlaceholderExpansion() {
    override fun getIdentifier(): String = plugin.pluginMeta.name.lowercase()

    override fun getAuthor(): String = plugin.pluginMeta.authors.joinToString()

    override fun getVersion(): String = plugin.pluginMeta.version

    override fun persist(): Boolean = true

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        val joinQuitMessagesHandler = plugin.joinQuitMessagesHandler
        if (params.startsWith("total_join_messages", ignoreCase = true)) return joinQuitMessagesHandler.getJoinMessages().size.toString()
        if (params.startsWith("total_quit_messages", ignoreCase = true)) return joinQuitMessagesHandler.getQuitMessages().size.toString()
        if (params.startsWith("total_messages", ignoreCase = true)) return joinQuitMessagesHandler.getAllMessages().size.toString()

        if (player == null) return null

        val user = plugin.usersHandler.getIfLoaded(player.uniqueId) ?: return null
        if (params.equals("show_join_quit_messages", ignoreCase = true)) return if (user.showJoinQuitMessages) PlaceholderAPIPlugin.booleanTrue() else PlaceholderAPIPlugin.booleanFalse()
        if (params.equals("join_message_selected", ignoreCase = true)) return if (user.joinMessage != null) PlaceholderAPIPlugin.booleanTrue() else PlaceholderAPIPlugin.booleanFalse()
        if (params.equals("quit_message_selected", ignoreCase = true)) return if (user.quitMessage != null) PlaceholderAPIPlugin.booleanTrue() else PlaceholderAPIPlugin.booleanFalse()
        if (params.equals("join_messages_unlocked", ignoreCase = true)) return joinQuitMessagesHandler.getJoinMessages().count { it.hasPermission(player) }.toString()
        if (params.equals("quit_messages_unlocked", ignoreCase = true)) return joinQuitMessagesHandler.getQuitMessages().count { it.hasPermission(player) }.toString()

        return null
    }
}