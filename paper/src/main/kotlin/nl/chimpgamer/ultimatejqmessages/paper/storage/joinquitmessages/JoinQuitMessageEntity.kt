package nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages

import com.google.common.base.Objects
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessage
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class JoinQuitMessageEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<JoinQuitMessageEntity>(JoinQuitMessagesTable)

    var name by JoinQuitMessagesTable.name
    var type by JoinQuitMessagesTable.type
    var message by JoinQuitMessagesTable.message
    var permission by JoinQuitMessagesTable.permission

    override fun hashCode(): Int = Objects.hashCode(name, type, message, permission)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JoinQuitMessageEntity) return false
        return name == other.name && type == other.type && message == other.message && permission == other.permission
    }
}

fun JoinQuitMessageEntity.toJoinQuitMessage() = JoinQuitMessage(id.value, name, type, message, permission)