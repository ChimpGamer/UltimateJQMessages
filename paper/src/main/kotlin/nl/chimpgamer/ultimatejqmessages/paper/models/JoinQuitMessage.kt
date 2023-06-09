package nl.chimpgamer.ultimatejqmessages.paper.models

import com.google.common.base.Objects
import nl.chimpgamer.ultimatejqmessages.paper.tables.JoinQuitMessagesTable
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class JoinQuitMessage(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<JoinQuitMessage>(JoinQuitMessagesTable)

    var name by JoinQuitMessagesTable.name
    var type by JoinQuitMessagesTable.type
    var message by JoinQuitMessagesTable.message

    fun hasPermission(player: Player) = player.hasPermission("ultimatejqmessages.access.$name") || player.hasPermission("ultimatejqmessages.access.$id")

    override fun hashCode(): Int = Objects.hashCode(name, type, message)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JoinQuitMessage) return false
        return name == other.name && type == other.type && message == other.message
    }
}

enum class JoinQuitMessageType {
    JOIN,
    QUIT
}