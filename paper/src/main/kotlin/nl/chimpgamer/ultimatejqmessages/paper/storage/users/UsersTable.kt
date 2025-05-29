package nl.chimpgamer.ultimatejqmessages.paper.storage.users

import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.JoinQuitMessagesTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption

object UsersTable : UUIDTable("users", "uuid") {
    val playerName: Column<String> = varchar("player_name", 16)
    val joinMessage = reference("join_message", JoinQuitMessagesTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val quitMessage = reference("quit_message", JoinQuitMessagesTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val customJoinMessage = text("custom_join_message").nullable()
    val customQuitMessage = text("custom_quit_message").nullable()
    val showJoinQuitMessages = bool("show_join_quit_messages").default(true)
    var randomJoinQuitMessages = bool("random_join_quit_messages").default(false)
}