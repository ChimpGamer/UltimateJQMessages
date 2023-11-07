package nl.chimpgamer.ultimatejqmessages.paper.models

import org.bukkit.entity.Player

data class JoinQuitMessage(
    val id: Int?,
    val name: String,
    val type: JoinQuitMessageType,
    val message: String,
    val permission: String? = null
) {
    fun hasPermission(player: Player) = if (permission == null)
        player.hasPermission("ultimatejqmessages.access.$name") || player.hasPermission("ultimatejqmessages.access.$id")
    else player.hasPermission(permission)
}

enum class JoinQuitMessageType {
    JOIN,
    QUIT
}