package nl.chimpgamer.ultimatejqmessages.paper.handlers

import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.events.ClearJoinQuitMessageEvent
import nl.chimpgamer.ultimatejqmessages.paper.events.JoinQuitMessageSelectEvent
import nl.chimpgamer.ultimatejqmessages.paper.events.SetCustomJoinQuitMessageEvent
import nl.chimpgamer.ultimatejqmessages.paper.events.ToggleShowJoinQuitMessagesEvent
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessage
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import nl.chimpgamer.ultimatejqmessages.paper.models.User
import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.JoinQuitMessageEntity
import nl.chimpgamer.ultimatejqmessages.paper.storage.users.UserEntity
import nl.chimpgamer.ultimatejqmessages.paper.storage.users.toUser
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class UsersHandler(private val plugin: UltimateJQMessagesPlugin) {
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
        } else if (userEntity.playerName != playerName) {
            transaction {
                userEntity.playerName = playerName
            }
        }
        users[playerUUID] = userEntity.toUser()
    }

    fun unloadUser(playerUUID: UUID) = users.remove(playerUUID)

    fun getIfLoaded(uuid: UUID) = users[uuid]

    suspend fun reload() = users.keys.forEach {
        val user = newSuspendedTransaction {
            UserEntity.findById(it)?.load(UserEntity::joinMessage, UserEntity::quitMessage)
        }?.toUser()
        if (user != null) users.replace(it, user)
    }

    suspend fun reload(playerUUID: UUID) {
        val user = newSuspendedTransaction {
            UserEntity.findById(playerUUID)?.load(UserEntity::joinMessage, UserEntity::quitMessage)
        }?.toUser()
        if (user != null) users.replace(playerUUID, user)
    }

    suspend fun setJoinMessage(user: User, joinQuitMessage: JoinQuitMessage?) {
        user.joinMessage = joinQuitMessage
        newSuspendedTransaction {
            val userEntity = UserEntity[user.uuid]
            val joinQuitMessageEntity = if (joinQuitMessage != null) JoinQuitMessageEntity[joinQuitMessage.id!!] else null
            userEntity.joinMessage = joinQuitMessageEntity
        }
        JoinQuitMessageSelectEvent(user, joinQuitMessage).callEvent()
    }

    suspend fun setQuitMessage(user: User, joinQuitMessage: JoinQuitMessage?) {
        user.quitMessage = joinQuitMessage
        newSuspendedTransaction {
            val userEntity = UserEntity[user.uuid]
            val joinQuitMessageEntity = if (joinQuitMessage != null) JoinQuitMessageEntity[joinQuitMessage.id!!] else null
            userEntity.quitMessage = joinQuitMessageEntity
        }
        JoinQuitMessageSelectEvent(user, joinQuitMessage).callEvent()
    }

    suspend fun setCustomJoinMessage(user: User, customJoinMessage: String) {
        user.customJoinMessage = customJoinMessage
        newSuspendedTransaction {
            val userEntity = UserEntity[user.uuid]
            userEntity.customJoinMessage = customJoinMessage
        }
        SetCustomJoinQuitMessageEvent(user, JoinQuitMessageType.JOIN, customJoinMessage).callEvent()
    }

    suspend fun setCustomQuitMessage(user: User, customQuitMessage: String) {
        user.customQuitMessage = customQuitMessage
        newSuspendedTransaction {
            val userEntity = UserEntity[user.uuid]
            userEntity.customQuitMessage = customQuitMessage
        }
        SetCustomJoinQuitMessageEvent(user, JoinQuitMessageType.QUIT, customQuitMessage).callEvent()
    }

    suspend fun clearJoinMessages(user: User) {
        user.apply {
            joinMessage = null
            customJoinMessage = null
        }
        newSuspendedTransaction {
            val userEntity = UserEntity[user.uuid]
            userEntity.apply {
                joinMessage = null
                customJoinMessage = null
            }
        }
        ClearJoinQuitMessageEvent(user, JoinQuitMessageType.JOIN).callEvent()
    }

    suspend fun clearQuitMessages(user: User) {
        user.apply {
            quitMessage = null
            customQuitMessage = null
        }
        newSuspendedTransaction {
            val userEntity = UserEntity[user.uuid]
            userEntity.apply {
                quitMessage = null
                customQuitMessage = null
            }
        }
        ClearJoinQuitMessageEvent(user, JoinQuitMessageType.QUIT).callEvent()
    }

    suspend fun setShowJoinQuitMessages(user: User, showJoinQuitMessages: Boolean) {
        user.showJoinQuitMessages = showJoinQuitMessages
        newSuspendedTransaction {
            val userEntity = UserEntity[user.uuid]
            userEntity.showJoinQuitMessages = showJoinQuitMessages
        }
        ToggleShowJoinQuitMessagesEvent(user, showJoinQuitMessages).callEvent()
    }

    fun getUsers(): Collection<User> = users.values.toSet()
}