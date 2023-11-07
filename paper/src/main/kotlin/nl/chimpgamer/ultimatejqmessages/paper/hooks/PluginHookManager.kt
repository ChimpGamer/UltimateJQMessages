package nl.chimpgamer.ultimatejqmessages.paper.hooks

import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent

class PluginHookManager(plugin: UltimateJQMessagesPlugin) : Listener {
    private val placeholderAPIHook = PlaceholderAPIHook(plugin)
    private val miniPlaceholdersHook = MiniPlaceholdersHook(plugin)

    fun load() {
        placeholderAPIHook.load()
        miniPlaceholdersHook.load()
    }

    fun unload() {
        placeholderAPIHook.unload()
        miniPlaceholdersHook.unload()
    }

    @EventHandler
    fun PluginEnableEvent.onPluginEnable() {
        if (plugin.name == "PlaceholderAPI") {
            placeholderAPIHook.load()
        } else if (plugin.name == "MiniPlaceholders") {
            miniPlaceholdersHook.load()
        }
    }

    @EventHandler
    fun PluginDisableEvent.onPluginDisable() {
        if (plugin.name == "PlaceholderAPI") {
            placeholderAPIHook.unload()
        } else if (plugin.name == "MiniPlaceholders") {
            miniPlaceholdersHook.unload()
        }
    }
}