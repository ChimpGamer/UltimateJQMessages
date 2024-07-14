package nl.chimpgamer.ultimatejqmessages.paper.handlers

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.CoroutineStart
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.batchInsertOnDuplicateKeyUpdate
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessage
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.JoinQuitMessageEntity
import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.JoinQuitMessagesTable
import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.toJoinQuitMessage
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

class JoinQuitMessagesHandler(private val plugin: UltimateJQMessagesPlugin) {
    private val joinQuitMessages: MutableMap<String, JoinQuitMessage> = ConcurrentHashMap()

    private val databaseDispatcher get() = plugin.dataHandler.databaseDispatcher

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

    fun createJoinQuitMessage(
        name: String,
        type: JoinQuitMessageType,
        message: String,
        permission: String? = null
    ): JoinQuitMessage {
        val joinQuitMessage = transaction {
            JoinQuitMessageEntity.new {
                this.name = name
                this.type = type
                this.message = message
                this.permission = permission
            }
        }.toJoinQuitMessage()
        joinQuitMessages[joinQuitMessage.name] = joinQuitMessage
        return joinQuitMessage
    }

    fun insertOrReplace(joinQuitMessage: JoinQuitMessage) {
        return transaction {
            JoinQuitMessagesTable.batchInsertOnDuplicateKeyUpdate(
                listOf(joinQuitMessage),
                listOf(JoinQuitMessagesTable.name, JoinQuitMessagesTable.type, JoinQuitMessagesTable.message, JoinQuitMessagesTable.permission)
            ) { batch, joinQuitMessage ->
                batch[name] = joinQuitMessage.name
                batch[type] = joinQuitMessage.type
                batch[message] = joinQuitMessage.message
                batch[permission] = joinQuitMessage.permission
            }
        }
    }

    suspend fun deleteJoinQuitMessage(joinQuitMessage: JoinQuitMessage) {
        val id = joinQuitMessage.id
        if (id != null) {
            newSuspendedTransaction(databaseDispatcher) {
                JoinQuitMessageEntity[id].delete()
            }
        }
        joinQuitMessages.remove(joinQuitMessage.name)
        updateUsersWithJoinQuitMessage(joinQuitMessage)
    }

    suspend fun setMessage(joinQuitMessage: JoinQuitMessage, message: String) {
        joinQuitMessage.message = message

        newSuspendedTransaction(databaseDispatcher) {
            val joinQuitMessageEntity = JoinQuitMessageEntity[joinQuitMessage.id!!]
            joinQuitMessageEntity.message = message
        }

        updateUsersWithJoinQuitMessage(joinQuitMessage)
    }

    suspend fun setPermission(joinQuitMessage: JoinQuitMessage, permission: String) {
        joinQuitMessage.permission = permission

        newSuspendedTransaction(databaseDispatcher) {
            val joinQuitMessageEntity = JoinQuitMessageEntity[joinQuitMessage.id!!]
            joinQuitMessageEntity.permission = permission
        }

        updateUsersWithJoinQuitMessage(joinQuitMessage)
    }

    private fun updateUsersWithJoinQuitMessage(joinQuitMessage: JoinQuitMessage) {
        plugin.launch(plugin.asyncDispatcher, CoroutineStart.UNDISPATCHED) {
            plugin.usersHandler.getUsers().filter { user -> user.joinMessage?.id == joinQuitMessage.id || user.quitMessage?.id == joinQuitMessage.id }.forEach { plugin.usersHandler.reload(it.uuid) }
        }
    }

    fun getJoinQuitMessageByName(name: String): JoinQuitMessage? {
        return joinQuitMessages[name]
    }

    fun getJoinMessages(): Collection<JoinQuitMessage> {
        return joinQuitMessages.filterValues { it.type === JoinQuitMessageType.JOIN }.values
    }

    fun getJoinMessagesSorted(): Collection<JoinQuitMessage> = getJoinMessages().sortedBy { it.name }

    fun getQuitMessages(): Collection<JoinQuitMessage> {
        return joinQuitMessages.filterValues { it.type === JoinQuitMessageType.QUIT }.values
    }

    fun getQuitMessagesSorted(): Collection<JoinQuitMessage> = getQuitMessages().sortedBy { it.name }

    fun getAllMessages(): Collection<JoinQuitMessage> {
        return joinQuitMessages.values
    }
}