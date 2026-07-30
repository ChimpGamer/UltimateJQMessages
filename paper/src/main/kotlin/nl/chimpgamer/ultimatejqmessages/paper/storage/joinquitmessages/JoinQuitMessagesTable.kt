package nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages

import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object JoinQuitMessagesTable : IntIdTable("join_quit_messages") {
    val name: Column<String> = varchar("name", 32).uniqueIndex()
    val type: Column<JoinQuitMessageType> = enumeration("type")
    val message: Column<String> = text("message")
    val permission: Column<String?> = varchar("permission", 200).nullable()
}