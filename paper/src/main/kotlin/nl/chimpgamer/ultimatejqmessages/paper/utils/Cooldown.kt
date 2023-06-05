package nl.chimpgamer.networkmanager.api.utils

import java.util.*

class Cooldown(private val id: UUID, private val cooldownName: String, private val timeInSeconds: Int) {
    private var start: Long = 0
    fun start() {
        start = System.currentTimeMillis()
        cooldowns[id.toString() + cooldownName] = this
    }

    companion object {
        private val cooldowns = HashMap<String, Cooldown>()

        @JvmStatic
        fun isInCooldown(id: UUID, cooldownName: String): Boolean {
            return if (getTimeLeft(id, cooldownName) >= 1) {
                true
            } else {
                stop(id, cooldownName)
                false
            }
        }

        private fun stop(id: UUID, cooldownName: String) {
            cooldowns.remove(id.toString() + cooldownName)
        }

        private fun getCooldown(id: UUID, cooldownName: String): Cooldown? = cooldowns[id.toString() + cooldownName]

        @JvmStatic
        fun getTimeLeft(id: UUID, cooldownName: String): Int {
            val cooldown = getCooldown(id, cooldownName)
            var f = -1
            if (cooldown != null) {
                val now = System.currentTimeMillis()
                val cooldownTime = cooldown.start
                val r = (now - cooldownTime).toInt() / 1000
                f = (r - cooldown.timeInSeconds) * -1
            }
            return f
        }
    }
}
