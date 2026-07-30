package nl.chimpgamer.ultimatejqmessages.paper.handlers

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import kotlinx.coroutines.withContext
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.events.JoinQuitMessageCreateEvent
import nl.chimpgamer.ultimatejqmessages.paper.events.JoinQuitMessageDeleteEvent
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessage
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.JoinQuitMessageEntity
import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.JoinQuitMessagesTable
import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.toJoinQuitMessage
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import java.util.concurrent.ConcurrentHashMap

class JoinQuitMessagesHandler(private val plugin: UltimateJQMessagesPlugin) {
    private val joinQuitMessages: MutableMap<String, JoinQuitMessage> = ConcurrentHashMap()

    fun load() {
        val loadedJoinQuitMessages = HashMap<String, JoinQuitMessage>()
        transaction {
            loadedJoinQuitMessages.putAll(JoinQuitMessageEntity.all().map { it.toJoinQuitMessage() }
                .map { it.name to it })
        }
        joinQuitMessages.clear()
        joinQuitMessages.putAll(loadedJoinQuitMessages)
        plugin.logger.info("Loaded ${joinQuitMessages.size} join quit messages from the database.")
    }

    suspend fun createJoinQuitMessage(
        name: String,
        type: JoinQuitMessageType,
        message: String,
        permission: String? = null
    ): JoinQuitMessage {
        val joinQuitMessage = withContext(plugin.asyncDispatcher) {
            suspendTransaction {
                JoinQuitMessageEntity.new {
                    this.name = name
                    this.type = type
                    this.message = message
                    this.permission = permission
                }
            }
        }.toJoinQuitMessage()
        joinQuitMessages[joinQuitMessage.name] = joinQuitMessage
        JoinQuitMessageCreateEvent(joinQuitMessage).callEvent()
        return joinQuitMessage
    }

    suspend fun insertOrReplace(joinQuitMessage: JoinQuitMessage) {
        return withContext(plugin.asyncDispatcher) {
            suspendTransaction {
                val keys = arrayOf(
                    JoinQuitMessagesTable.name,
                    JoinQuitMessagesTable.type,
                    JoinQuitMessagesTable.message,
                    JoinQuitMessagesTable.permission
                )
                JoinQuitMessagesTable.upsert(keys = keys) {
                    it[name] = joinQuitMessage.name
                    it[type] = joinQuitMessage.type
                    it[message] = joinQuitMessage.message
                    it[permission] = joinQuitMessage.permission
                }
            }
        }
    }

    suspend fun deleteJoinQuitMessage(joinQuitMessage: JoinQuitMessage) {
        val id = joinQuitMessage.id
        if (id != null) {
            withContext(plugin.asyncDispatcher) {
                suspendTransaction {
                    JoinQuitMessageEntity[id].delete()
                }
            }
        }
        joinQuitMessages.remove(joinQuitMessage.name)
        JoinQuitMessageDeleteEvent(joinQuitMessage).callEvent()
        updateUsersWithJoinQuitMessage(joinQuitMessage)
    }

    suspend fun setMessage(joinQuitMessage: JoinQuitMessage, message: String) {
        joinQuitMessage.message = message

        withContext(plugin.asyncDispatcher) {
            suspendTransaction {
                val joinQuitMessageEntity = JoinQuitMessageEntity[joinQuitMessage.id!!]
                joinQuitMessageEntity.message = message
            }
        }

        updateUsersWithJoinQuitMessage(joinQuitMessage)
    }

    suspend fun setPermission(joinQuitMessage: JoinQuitMessage, permission: String) {
        joinQuitMessage.permission = permission

        withContext(plugin.asyncDispatcher) {
            suspendTransaction {
                val joinQuitMessageEntity = JoinQuitMessageEntity[joinQuitMessage.id!!]
                joinQuitMessageEntity.permission = permission
            }
        }

        updateUsersWithJoinQuitMessage(joinQuitMessage)
    }

    private suspend fun updateUsersWithJoinQuitMessage(joinQuitMessage: JoinQuitMessage) =
        withContext(plugin.asyncDispatcher) {
            plugin.usersHandler.getUsers()
                .filter { user -> user.joinMessage?.id == joinQuitMessage.id || user.quitMessage?.id == joinQuitMessage.id }
                .forEach { plugin.usersHandler.reload(it.uuid) }
        }

    suspend fun exists(name: String): Boolean = joinQuitMessages.containsKey(name) || existsInDatebase(name)

    private suspend fun existsInDatebase(name: String): Boolean {
        return withContext(plugin.asyncDispatcher) {
            suspendTransaction {
                !JoinQuitMessageEntity.find { JoinQuitMessagesTable.name eq name }.empty()
            }
        }
    }

    fun getJoinMessageByName(name: String): JoinQuitMessage? {
        val message = joinQuitMessages[name] ?: return null
        return if (message.type.isJoin()) message else null
    }

    fun getQuitMessageByName(name: String): JoinQuitMessage? {
        val message = joinQuitMessages[name] ?: return null
        return if (message.type.isQuit()) message else null
    }

    fun getJoinQuitMessageByName(name: String): JoinQuitMessage? {
        return joinQuitMessages[name]
    }

    fun getJoinMessages(): Collection<JoinQuitMessage> {
        return joinQuitMessages.filterValues { it.type.isJoin() }.values
    }

    fun getJoinMessagesSorted(): Collection<JoinQuitMessage> = getJoinMessages().sortedBy { it.name }

    fun getQuitMessages(): Collection<JoinQuitMessage> {
        return joinQuitMessages.filterValues { it.type.isQuit() }.values
    }

    fun getQuitMessagesSorted(): Collection<JoinQuitMessage> = getQuitMessages().sortedBy { it.name }

    fun getAllMessages(): Collection<JoinQuitMessage> {
        return joinQuitMessages.values
    }

    fun randomJoinMessage(player: Player): JoinQuitMessage? =
        getJoinMessages().filter { it.hasPermission(player) }.randomOrNull()

    fun randomQuitMessage(player: Player): JoinQuitMessage? =
        getQuitMessages().filter { it.hasPermission(player) }.randomOrNull()
}