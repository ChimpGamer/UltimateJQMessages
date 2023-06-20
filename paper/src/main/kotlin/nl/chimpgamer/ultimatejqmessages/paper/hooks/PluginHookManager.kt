package nl.chimpgamer.ultimatejqmessages.paper.hooks

import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent

class PluginHookManager(private val plugin: UltimateJQMessagesPlugin) : Listener {
    private val placeholderAPIHook = PlaceholderAPIHook(plugin)

    fun load() {
        placeholderAPIHook.load()
    }

    fun unload() {
        placeholderAPIHook.unload()
    }

    @EventHandler
    fun PluginEnableEvent.onPluginEnable() {
        if (plugin.name == "PlaceholderAPI") {
            placeholderAPIHook.load()
        }
    }

    @EventHandler
    fun PluginDisableEvent.onPluginDisable() {
        if (plugin.name == "PlaceholderAPI") {
            placeholderAPIHook.unload()
        }
    }
}