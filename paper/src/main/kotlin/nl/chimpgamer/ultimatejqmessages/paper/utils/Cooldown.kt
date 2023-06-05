package nl.chimpgamer.ultimatejqmessages.paper.utils

import java.time.Duration
import java.time.Instant
import java.util.UUID

class Cooldown(private val id: UUID, private val cooldownName: String, private val duration: Duration) {
    private var until: Instant = Instant.MIN

    fun start() {
        until = Instant.now().plus(duration)
        cooldowns[id.toString() + cooldownName] = this
    }

    companion object {
        private val cooldowns = HashMap<String, Cooldown>()

        fun hasCooldown(id: UUID, cooldownName: String): Boolean {
            val cooldownUntil = cooldowns[id.toString() + cooldownName]?.until
            return cooldownUntil != null && Instant.now().isBefore(cooldownUntil)
        }

        private fun stop(id: UUID, cooldownName: String) {
            cooldowns.remove(id.toString() + cooldownName)
        }

        fun getRemainingTime(id: UUID, cooldownName: String): Duration {
            val cooldownUntil = cooldowns[id.toString() + cooldownName]?.until
            val now = Instant.now()
            return if (cooldownUntil != null && now.isBefore(cooldownUntil)) {
                Duration.between(now, cooldownUntil)
            } else {
                Duration.ZERO
            }
        }
    }
}
