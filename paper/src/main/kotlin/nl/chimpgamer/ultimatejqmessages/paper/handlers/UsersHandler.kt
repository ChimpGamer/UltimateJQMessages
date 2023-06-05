package nl.chimpgamer.ultimatejqmessages.paper.handlers

import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.models.User
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class UsersHandler(private val plugin: UltimateJQMessagesPlugin) {

    fun loadUser(playerUUID: UUID, playerName: String) {
        val user = transaction { User.findById(playerUUID)?.load(User::joinMessage, User::quitMessage) }
        if (user == null) {
            transaction {
                User.new(playerUUID) {
                    this.playerName = playerName
                }
            }
        }
    }

    fun getUser(playerUUID: UUID): User? {
        return transaction { User.findById(playerUUID)?.load(User::joinMessage, User::quitMessage) }
    }
}