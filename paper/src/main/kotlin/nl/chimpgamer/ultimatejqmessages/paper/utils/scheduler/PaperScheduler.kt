package nl.chimpgamer.ultimatejqmessages.paper.utils.scheduler

import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class PaperScheduler(private val plugin: UltimateJQMessagesPlugin) {

    private val executor = Executor { r: Runnable -> bukkitScheduler.runTaskAsynchronously(bootstrap, r) }
    private val bootstrap: UltimateJQMessagesPlugin get() = plugin
    private val bukkitScheduler get() = bootstrap.server.scheduler

    fun runAsync(runnable: Runnable) {
        bukkitScheduler.runTaskAsynchronously(bootstrap, runnable)
    }

    fun runSync(runnable: Runnable) {
        bukkitScheduler.runTask(bootstrap, runnable)
    }

    fun runDelayed(runnable: Runnable, delay: Long, timeUnit: TimeUnit): Int {
        return bukkitScheduler.runTaskLater(bootstrap, runnable, timeUnit.toSeconds(delay) * 20).taskId
    }

    fun runRepeating(runnable: Runnable, period: Long, timeUnit: TimeUnit): Int {
        return bukkitScheduler.runTaskTimer(bootstrap, runnable, 0, timeUnit.toSeconds(period) * 20).taskId
    }

    fun stopTask(taskId: Int) {
        bukkitScheduler.cancelTask(taskId)
    }

    fun async(): Executor = executor
}