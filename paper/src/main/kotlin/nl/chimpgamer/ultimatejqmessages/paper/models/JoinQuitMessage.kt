package nl.chimpgamer.ultimatejqmessages.paper.models

import org.bukkit.entity.Player

data class JoinQuitMessage(
    val id: Int?,
    val name: String,
    val type: JoinQuitMessageType,
    val message: String
) {
    fun hasPermission(player: Player) = player.hasPermission("ultimatejqmessages.access.$name") || player.hasPermission("ultimatejqmessages.access.$id")
}

enum class JoinQuitMessageType {
    JOIN,
    QUIT
}