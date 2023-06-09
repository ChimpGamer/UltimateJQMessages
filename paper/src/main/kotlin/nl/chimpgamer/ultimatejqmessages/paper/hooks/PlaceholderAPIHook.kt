package nl.chimpgamer.ultimatejqmessages.paper.hooks

import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import org.bukkit.entity.Player

class PlaceholderAPIHook(private val plugin: UltimateJQMessagesPlugin) {
    private lateinit var expansion: PapiPlaceholderExpansion

    private val isEnabled get() = plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")

    fun load() {
        if (isEnabled) {
            expansion = PapiPlaceholderExpansion(plugin).also { it.register() }
            plugin.logger.info("Hooked into PlaceholderAPI")
        }
    }

    fun unload() {
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
        if (player == null) return null

        val user = plugin.usersHandler.getUser(player.uniqueId) ?: return null
        if (params.equals("show_join_quit_messages", ignoreCase = true)) return if (user.showJoinQuitMessages) PlaceholderAPIPlugin.booleanTrue() else PlaceholderAPIPlugin.booleanFalse()
        if (params.equals("join_message_selected", ignoreCase = true)) return if (user.joinMessage != null) PlaceholderAPIPlugin.booleanTrue() else PlaceholderAPIPlugin.booleanFalse()
        if (params.equals("quit_message_selected", ignoreCase = true)) return if (user.quitMessage != null) PlaceholderAPIPlugin.booleanTrue() else PlaceholderAPIPlugin.booleanFalse()

        return null
    }
}