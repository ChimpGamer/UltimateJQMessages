package nl.chimpgamer.ultimatejqmessages.paper.handlers

import kotlinx.coroutines.Dispatchers
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessage
import nl.chimpgamer.ultimatejqmessages.paper.models.User
import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.JoinQuitMessageEntity
import nl.chimpgamer.ultimatejqmessages.paper.storage.users.UserEntity
import nl.chimpgamer.ultimatejqmessages.paper.storage.users.toUser
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class UsersHandler {
    private val users: MutableMap<UUID, User> = ConcurrentHashMap()

    fun loadUser(playerUUID: UUID, playerName: String) {
        var userEntity =
            transaction { UserEntity.findById(playerUUID)?.load(UserEntity::joinMessage, UserEntity::quitMessage) }
        if (userEntity == null) {
            userEntity = transaction {
                UserEntity.new(playerUUID) {
                    this.playerName = playerName
                }.load(UserEntity::joinMessage, UserEntity::quitMessage)
            }
        }
        users[playerUUID] = userEntity.toUser()
    }

    fun unloadUser(playerUUID: UUID) = users.remove(playerUUID)

    fun getIfLoaded(uuid: UUID) = users[uuid]

    suspend fun setJoinMessage(user: User, joinQuitMessage: JoinQuitMessage) {
        user.joinMessage = joinQuitMessage
        newSuspendedTransaction(Dispatchers.IO) {
            val userEntity = UserEntity[user.uuid]
            val joinQuitMessageEntity = JoinQuitMessageEntity[joinQuitMessage.id!!]
            userEntity.joinMessage = joinQuitMessageEntity
        }
    }

    suspend fun setQuitMessage(user: User, joinQuitMessage: JoinQuitMessage) {
        user.quitMessage = joinQuitMessage
        newSuspendedTransaction(Dispatchers.IO) {
            val userEntity = UserEntity[user.uuid]
            val joinQuitMessageEntity = JoinQuitMessageEntity[joinQuitMessage.id!!]
            userEntity.quitMessage = joinQuitMessageEntity
        }
    }

    suspend fun setCustomJoinMessage(user: User, customJoinMessage: String) {
        user.customJoinMessage = customJoinMessage
        newSuspendedTransaction(Dispatchers.IO) {
            val userEntity = UserEntity[user.uuid]
            userEntity.customJoinMessage = customJoinMessage
        }
    }

    suspend fun setCustomQuitMessage(user: User, customQuitMessage: String) {
        user.customQuitMessage = customQuitMessage
        newSuspendedTransaction(Dispatchers.IO) {
            val userEntity = UserEntity[user.uuid]
            userEntity.customQuitMessage = customQuitMessage
        }
    }

    suspend fun clearJoinMessages(user: User) {
        user.apply {
            joinMessage = null
            customJoinMessage = null
        }
        newSuspendedTransaction(Dispatchers.IO) {
            val userEntity = UserEntity[user.uuid]
            userEntity.apply {
                joinMessage = null
                customJoinMessage = null
            }
        }
    }

    suspend fun clearQuitMessages(user: User) {
        user.apply {
            quitMessage = null
            customQuitMessage = null
        }
        newSuspendedTransaction(Dispatchers.IO) {
            val userEntity = UserEntity[user.uuid]
            userEntity.apply {
                quitMessage = null
                customQuitMessage = null
            }
        }
    }

    suspend fun setShowJoinQuitMessages(user: User, showJoinQuitMessages: Boolean) {
        user.showJoinQuitMessages = showJoinQuitMessages
        newSuspendedTransaction(Dispatchers.IO) {
            val userEntity = UserEntity[user.uuid]
            userEntity.showJoinQuitMessages = showJoinQuitMessages
        }
    }
}