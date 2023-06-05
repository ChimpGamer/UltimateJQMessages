package nl.chimpgamer.ultimatejqmessages.paper.utils

import org.bukkit.entity.Player

object StringUtils {
    private val placeholderRegex = Regex("%([^%]+)%")

    fun containsPlaceholder(text: String) = placeholderRegex.containsMatchIn(text)

    fun applyPlaceholders(text: String, player: Player?): String {
        var result = text
        if (player != null) {
            result = result
                .replace("%player_name%", player.name)
                .replace("%player_uuid%", player.uniqueId.toString())
                .replace("%player_world%", player.world.name)
        }

        return result
    }
}