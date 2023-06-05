package nl.chimpgamer.ultimatejqmessages.paper.tables

import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object MessagesTable : IntIdTable("messages") {
    val name: Column<String> = varchar("name", 32).uniqueIndex()
    val type: Column<JoinQuitMessageType> = enumeration("type")
    val message: Column<String> = text("message")
}