package nl.chimpgamer.ultimatejqmessages.paper.commands

import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.parse
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.CommandManager
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser.offlinePlayerParser
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.kotlin.extension.cloudKey
import kotlin.jvm.optionals.getOrNull

class QuitMessagesCommand(private val plugin: UltimateJQMessagesPlugin) {

    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val basePermission = "ultimatejqmessages.command.quitmessages"
        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermission)

        val playerKey = cloudKey<OfflinePlayer>("player")

        commandManager.command(builder
            .senderType(Player::class.java)
            .handler { context ->
                val sender = context.sender()
                // Open menu...
                plugin.quitMessageSelectorMenu.open(sender)
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .permission("$basePermission.reset")
            .literal("reset", "clear")
            .optional(playerKey, offlinePlayerParser())
            .suspendingHandler { context ->
                val sender = context.sender()
                val offlinePlayer = context.optional(playerKey).getOrNull()

                val usersHandler = plugin.usersHandler
                if (offlinePlayer == null) {
                    val user = usersHandler.getIfLoaded(sender.uniqueId) ?: return@suspendingHandler
                    usersHandler.clearQuitMessages(user)
                    sender.sendRichMessage(plugin.messagesConfig.quitMessageReset)
                } else {
                    val user = usersHandler.getIfLoaded(offlinePlayer.uniqueId)
                    if (user == null) {
                        sender.sendRichMessage("Could not find user ${offlinePlayer.name} in the database!")
                        return@suspendingHandler
                    }
                    usersHandler.clearQuitMessages(user)
                    sender.sendMessage(plugin.messagesConfig.quitMessageResetOther.parse(mapOf("displayname" to user.playerName)))
                }
            }
        )
    }
}