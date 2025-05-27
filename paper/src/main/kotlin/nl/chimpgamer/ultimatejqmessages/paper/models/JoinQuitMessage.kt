package nl.chimpgamer.ultimatejqmessages.paper.models

import org.bukkit.entity.Player

data class JoinQuitMessage(
    val id: Int?,
    val name: String,
    val type: JoinQuitMessageType,
    var message: String,
    var permission: String? = null
) {
    fun hasPermission(player: Player) = if (permission != null)
        player.hasPermission(permission!!)
    else player.hasPermission("ultimatejqmessages.access.$id") || player.hasPermission("ultimatejqmessages.access.$name")
}

enum class JoinQuitMessageType {
    JOIN,
    QUIT;

    fun isJoin() = this == JOIN
    fun isQuit() = this == QUIT
}