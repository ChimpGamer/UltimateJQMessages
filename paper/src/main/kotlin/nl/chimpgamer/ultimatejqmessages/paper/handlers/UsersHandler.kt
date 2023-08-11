package nl.chimpgamer.ultimatejqmessages.paper.handlers

import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.storage.users.UserEntity
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class UsersHandler(private val plugin: UltimateJQMessagesPlugin) {

    fun loadUser(playerUUID: UUID, playerName: String) {
        val userEntity = transaction { UserEntity.findById(playerUUID)?.load(UserEntity::joinMessage, UserEntity::quitMessage) }
        if (userEntity == null) {
            transaction {
                UserEntity.new(playerUUID) {
                    this.playerName = playerName
                }
            }
        }
    }

    fun getUser(playerUUID: UUID): UserEntity? {
        return transaction { UserEntity.findById(playerUUID)?.load(UserEntity::joinMessage, UserEntity::quitMessage) }
    }
}