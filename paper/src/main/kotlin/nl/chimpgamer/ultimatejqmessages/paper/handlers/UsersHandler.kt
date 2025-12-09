package nl.chimpgamer.ultimatejqmessages.paper.handlers

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
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
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class UsersHandler(private val plugin: UltimateJQMessagesPlugin) {
    private val users: MutableMap<UUID, User> = ConcurrentHashMap()

    suspend fun loadUser(playerUUID: UUID, playerName: String) = newSuspendedTransaction(plugin.asyncDispatcher) {
        val defaultJoinMessageName = plugin.settingsConfig.joinMessagesDefaultMessageName
        val defaultQuitMessageName = plugin.settingsConfig.quitMessagesDefaultMessageName
        val defaultJoinMessage = if (defaultJoinMessageName.isNotBlank()) plugin.joinQuitMessagesHandler.getJoinMessageByName(defaultJoinMessageName) else null
        val defaultQuitMessage = if (defaultQuitMessageName.isNotBlank()) plugin.joinQuitMessagesHandler.getQuitMessageByName(defaultQuitMessageName) else null

        var userEntity = UserEntity.findById(playerUUID)?.load(UserEntity::joinMessage, UserEntity::quitMessage)
        if (userEntity == null) {
            userEntity = UserEntity.new(playerUUID) {
                this.playerName = playerName
                if (defaultJoinMessage != null) this.joinMessage = JoinQuitMessageEntity[defaultJoinMessage.id!!]
                if (defaultQuitMessage != null) this.quitMessage = JoinQuitMessageEntity[defaultQuitMessage.id!!]
            }.load(UserEntity::joinMessage, UserEntity::quitMessage)
        } else {
            if (userEntity.playerName != playerName) {
                userEntity.playerName = playerName
            }
            if (!plugin.settingsConfig.joinMessagesDefaultMessageNewPlayersOnly && userEntity.joinMessage == null) {
                if (defaultJoinMessage != null) userEntity.joinMessage = JoinQuitMessageEntity[defaultJoinMessage.id!!]
            }
            if (!plugin.settingsConfig.quitMessagesDefaultMessageNewPlayersOnly && userEntity.quitMessage == null) {
                if (defaultQuitMessage != null) userEntity.quitMessage = JoinQuitMessageEntity[defaultQuitMessage.id!!]
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

    suspend fun reload(playerUUID: UUID) = newSuspendedTransaction(plugin.asyncDispatcher) {
        val user = UserEntity.findById(playerUUID)?.load(UserEntity::joinMessage, UserEntity::quitMessage)?.toUser()
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
        newSuspendedTransaction(plugin.asyncDispatcher) {
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
        newSuspendedTransaction(plugin.asyncDispatcher) {
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

    suspend fun setRandomJoinQuitMessages(user: User, randomJoinQuitMessage: Boolean) {
        user.randomJoinQuitMessages = randomJoinQuitMessage
        newSuspendedTransaction(plugin.asyncDispatcher) {
            val userEntity = UserEntity[user.uuid]
            userEntity.randomJoinQuitMessages = randomJoinQuitMessage
        }
    }

    fun getUsers(): Collection<User> = users.values.toSet()
}