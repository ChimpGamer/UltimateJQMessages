package nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages

import com.google.common.base.Objects
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessage
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class JoinQuitMessageEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<JoinQuitMessageEntity>(JoinQuitMessagesTable)

    var name by JoinQuitMessagesTable.name
    var type by JoinQuitMessagesTable.type
    var message by JoinQuitMessagesTable.message

    fun hasPermission(player: Player) = player.hasPermission("ultimatejqmessages.access.$name") || player.hasPermission("ultimatejqmessages.access.$id")

    override fun hashCode(): Int = Objects.hashCode(name, type, message)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JoinQuitMessageEntity) return false
        return name == other.name && type == other.type && message == other.message
    }
}

fun JoinQuitMessageEntity.toJoinQuitMessage() = JoinQuitMessage(id.value, name, type, message)