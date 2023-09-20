package nl.chimpgamer.ultimatejqmessages.paper.extensions

import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

/* Registers all of these listeners for the plugin. */
fun Plugin.registerEvents(
    vararg listeners: Listener
) = listeners.forEach { server.pluginManager.registerEvents(it, this) }

fun Plugin.runSync(runnable: Runnable) {
    if (server.isPrimaryThread) {
        runnable.run()
    } else {
        server.scheduler.runTask(this, runnable)
    }
}

fun Plugin.runAsync(runnable: Runnable) {
    if (!server.isPrimaryThread) {
        runnable.run()
    } else {
        server.scheduler.runTaskAsynchronously(this, runnable)
    }
}

fun Plugin.callEvent(event: Event) = server.pluginManager.callEvent(event)

fun Plugin.callEventSync(event: Event) = runSync { callEvent(event) }