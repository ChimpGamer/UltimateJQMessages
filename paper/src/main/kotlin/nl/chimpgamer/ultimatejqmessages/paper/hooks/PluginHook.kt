package nl.chimpgamer.ultimatejqmessages.paper.hooks

import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin

abstract class PluginHook(protected val plugin: UltimateJQMessagesPlugin, val pluginName: String) {
    open var isLoaded: Boolean = false
    abstract fun load()
    open fun unload() {}

    fun canHook(): Boolean {
        return plugin.server.pluginManager.isPluginEnabled(pluginName)
    }

    fun isPluginLoaded(): Boolean {
        return plugin.server.pluginManager.getPlugin(pluginName) != null
    }
}