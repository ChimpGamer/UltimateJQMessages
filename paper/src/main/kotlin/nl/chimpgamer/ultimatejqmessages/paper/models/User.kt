package nl.chimpgamer.ultimatejqmessages.paper.models

import nl.chimpgamer.ultimatejqmessages.paper.tables.UsersTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class User(uuid: EntityID<UUID>): UUIDEntity(uuid) {
    companion object : UUIDEntityClass<User>(UsersTable)

    var playerName by UsersTable.playerName
    var joinMessage by JoinQuitMessage optionalReferencedOn UsersTable.joinMessage
    var quitMessage by JoinQuitMessage optionalReferencedOn UsersTable.quitMessage
    var customJoinMessage by UsersTable.customJoinMessage
    var customQuitMessage by UsersTable.customQuitMessage
    var showJoinQuitMessages by UsersTable.showJoinQuitMessages

    fun joinMessage(joinQuitMessage: JoinQuitMessage?) = transaction {
        joinMessage = joinQuitMessage
    }

    fun quitMessage(joinQuitMessage: JoinQuitMessage?) = transaction {
        quitMessage = joinQuitMessage
    }

    fun customJoinMessage(joinMessage: String?) = transaction {
        customJoinMessage = joinMessage
    }

    fun customQuitMessage(quitMessage: String?) = transaction {
        customQuitMessage = quitMessage
    }

    fun clearJoinMessages() = transaction {
        joinMessage = null
        customJoinMessage = null
    }

    fun clearQuitMessages() = transaction {
        quitMessage = null
        customQuitMessage = null
    }

    fun showJoinQuitMessages(showJoinQuitMessages: Boolean) = transaction {
        this@User.showJoinQuitMessages = showJoinQuitMessages
    }
}