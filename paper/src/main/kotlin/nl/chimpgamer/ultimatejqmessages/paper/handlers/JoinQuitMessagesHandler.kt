package nl.chimpgamer.ultimatejqmessages.paper.handlers

import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.batchInsertOnDuplicateKeyUpdate
import nl.chimpgamer.ultimatejqmessages.paper.extensions.insertOnDuplicateKeyUpdate
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessage
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.JoinQuitMessageEntity
import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.JoinQuitMessagesTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class JoinQuitMessagesHandler(private val plugin: UltimateJQMessagesPlugin) {

    fun createJoinQuitMessage(name: String, type: JoinQuitMessageType, message: String): JoinQuitMessageEntity {
        return transaction {
            JoinQuitMessageEntity.new {
                this.name = name
                this.type = type
                this.message = message
            }
        }
    }

    fun insertOrReplace(joinQuitMessage: JoinQuitMessage) {
        return transaction {
            JoinQuitMessagesTable.batchInsertOnDuplicateKeyUpdate(listOf(joinQuitMessage), listOf(JoinQuitMessagesTable.name, JoinQuitMessagesTable.type, JoinQuitMessagesTable.message)) { batch, joinQuitMessage ->
                batch[name] = joinQuitMessage.name
                batch[type] = joinQuitMessage.type
                batch[message] = joinQuitMessage.message
            }
        }
    }

    fun deleteJoinQuitMessage(joinQuitMessageEntity: JoinQuitMessageEntity) {
        transaction {
            joinQuitMessageEntity.delete()
        }
    }

    fun getJoinQuitMessageByName(name: String): JoinQuitMessageEntity? {
        val predicate: (JoinQuitMessageEntity) -> Boolean = { joinQuitMessage -> joinQuitMessage.name == name }
        return transaction {
            JoinQuitMessageEntity.findWithCacheCondition(predicate) { JoinQuitMessagesTable.name eq name }.firstOrNull()
        }
    }

    fun getJoinMessageByName(name: String): JoinQuitMessageEntity? {
        val predicate: (JoinQuitMessageEntity) -> Boolean = { joinQuitMessage -> joinQuitMessage.name == name && joinQuitMessage.type == JoinQuitMessageType.QUIT }
        return transaction {
            JoinQuitMessageEntity.findWithCacheCondition(predicate) { JoinQuitMessagesTable.name eq name and (JoinQuitMessagesTable.type eq JoinQuitMessageType.QUIT) }.firstOrNull()
        }
    }

    fun getQuitMessageByName(name: String): JoinQuitMessageEntity? {
        val predicate: (JoinQuitMessageEntity) -> Boolean = { joinQuitMessage -> joinQuitMessage.name == name && joinQuitMessage.type == JoinQuitMessageType.QUIT }
        return transaction {
            JoinQuitMessageEntity.findWithCacheCondition(predicate) { JoinQuitMessagesTable.name eq name and (JoinQuitMessagesTable.type eq JoinQuitMessageType.QUIT) }.firstOrNull()
        }
    }

    fun getJoinMessages(): Set<JoinQuitMessageEntity> {
        val predicate: (JoinQuitMessageEntity) -> Boolean = { joinQuitMessage ->
            joinQuitMessage.type === JoinQuitMessageType.JOIN
        }
        return transaction {
            JoinQuitMessageEntity.findWithCacheCondition(predicate) { JoinQuitMessagesTable.type eq JoinQuitMessageType.JOIN }
                .toSet()
        }
    }

    fun getQuitMessages(): Set<JoinQuitMessageEntity> {
        val predicate: (JoinQuitMessageEntity) -> Boolean = { joinQuitMessage ->
            joinQuitMessage.type === JoinQuitMessageType.QUIT
        }
        return transaction {
            JoinQuitMessageEntity.findWithCacheCondition(predicate) { JoinQuitMessagesTable.type eq JoinQuitMessageType.QUIT }
                .toSet()
        }
    }

    fun getAllMessages(): Set<JoinQuitMessageEntity> {
        return transaction { JoinQuitMessageEntity.all().toSet() }
    }
}