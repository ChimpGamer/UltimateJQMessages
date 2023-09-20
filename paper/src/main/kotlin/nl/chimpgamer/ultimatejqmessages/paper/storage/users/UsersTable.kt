package nl.chimpgamer.ultimatejqmessages.paper.storage.users

import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.JoinQuitMessagesTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column

object UsersTable : UUIDTable("users", "uuid") {
    val playerName: Column<String> = varchar("player_name", 16)
    val joinMessage = reference("join_message", JoinQuitMessagesTable).nullable()
    val quitMessage = reference("quit_message", JoinQuitMessagesTable).nullable()
    val customJoinMessage = text("custom_join_message").nullable()
    val customQuitMessage = text("custom_quit_message").nullable()
    val showJoinQuitMessages = bool("show_join_quit_messages").default(true)
}