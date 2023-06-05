package nl.chimpgamer.ultimatejqmessages.paper.handlers

import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessage
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import nl.chimpgamer.ultimatejqmessages.paper.tables.JoinQuitMessagesTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class JoinQuitMessagesHandler(private val plugin: UltimateJQMessagesPlugin) {

    fun createJoinQuitMessage(name: String, type: JoinQuitMessageType, message: String): JoinQuitMessage {
        return transaction {
            JoinQuitMessage.new {
                this.name = name
                this.type = type
                this.message = message
            }
        }
    }

    fun deleteJoinQuitMessage(joinQuitMessage: JoinQuitMessage) {
        transaction {
            joinQuitMessage.delete()
        }
    }

    fun getJoinQuitMessageByName(name: String): JoinQuitMessage? {
        val predicate: (JoinQuitMessage) -> Boolean = { joinQuitMessage -> joinQuitMessage.name == name }
        return transaction {
            JoinQuitMessage.findWithCacheCondition(predicate) { JoinQuitMessagesTable.name eq name }.firstOrNull()
        }
    }

    fun getJoinMessageByName(name: String): JoinQuitMessage? {
        val predicate: (JoinQuitMessage) -> Boolean = { joinQuitMessage -> joinQuitMessage.name == name && joinQuitMessage.type == JoinQuitMessageType.QUIT }
        return transaction {
            JoinQuitMessage.findWithCacheCondition(predicate) { JoinQuitMessagesTable.name eq name and (JoinQuitMessagesTable.type eq JoinQuitMessageType.QUIT) }.firstOrNull()
        }
    }

    fun getQuitMessageByName(name: String): JoinQuitMessage? {
        val predicate: (JoinQuitMessage) -> Boolean = { joinQuitMessage -> joinQuitMessage.name == name && joinQuitMessage.type == JoinQuitMessageType.QUIT }
        return transaction {
            JoinQuitMessage.findWithCacheCondition(predicate) { JoinQuitMessagesTable.name eq name and (JoinQuitMessagesTable.type eq JoinQuitMessageType.QUIT) }.firstOrNull()
        }
    }

    fun getJoinMessages(): Set<JoinQuitMessage> {
        val predicate: (JoinQuitMessage) -> Boolean = { joinQuitMessage ->
            joinQuitMessage.type === JoinQuitMessageType.JOIN
        }
        return transaction {
            JoinQuitMessage.findWithCacheCondition(predicate) { JoinQuitMessagesTable.type eq JoinQuitMessageType.JOIN }
                .toSet()
        }
    }

    fun getQuitMessages(): Set<JoinQuitMessage> {
        val predicate: (JoinQuitMessage) -> Boolean = { joinQuitMessage ->
            joinQuitMessage.type === JoinQuitMessageType.QUIT
        }
        return transaction {
            JoinQuitMessage.findWithCacheCondition(predicate) { JoinQuitMessagesTable.type eq JoinQuitMessageType.QUIT }
                .toSet()
        }
    }
}