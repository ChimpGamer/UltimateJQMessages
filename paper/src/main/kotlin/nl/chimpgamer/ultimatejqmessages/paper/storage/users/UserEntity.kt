package nl.chimpgamer.ultimatejqmessages.paper.storage.users

import nl.chimpgamer.ultimatejqmessages.paper.models.User
import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.JoinQuitMessageEntity
import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.toJoinQuitMessage
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import java.util.*

class UserEntity(uuid: EntityID<UUID>): UUIDEntity(uuid) {
    companion object : UUIDEntityClass<UserEntity>(UsersTable)

    var playerName by UsersTable.playerName
    var joinMessage by JoinQuitMessageEntity optionalReferencedOn UsersTable.joinMessage
    var quitMessage by JoinQuitMessageEntity optionalReferencedOn UsersTable.quitMessage
    var customJoinMessage by UsersTable.customJoinMessage
    var customQuitMessage by UsersTable.customQuitMessage
    var showJoinQuitMessages by UsersTable.showJoinQuitMessages
    var randomJoinQuitMessages by UsersTable.randomJoinQuitMessages
}

fun UserEntity.toUser() = User(id.value, playerName, joinMessage?.toJoinQuitMessage(), quitMessage?.toJoinQuitMessage(), customJoinMessage, customQuitMessage, showJoinQuitMessages, randomJoinQuitMessages)