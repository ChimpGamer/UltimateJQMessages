package nl.chimpgamer.ultimatejqmessages.paper.commands

import cloud.commandframework.CommandManager
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.parse
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class JoinMessagesCommand(private val plugin: UltimateJQMessagesPlugin) {

    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val basePermission = "ultimatejqmessages.command.joinmessages"
        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermission)

        // Arguments
        val offlinePlayerArgument = OfflinePlayerArgument.optional<CommandSender>("player")

        commandManager.command(builder
            .senderType(Player::class.java)
            .handler { context ->
                val sender = context.sender as Player
                // Open menu...
                plugin.joinMessageSelectorMenu.open(sender)
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .permission("$basePermission.reset")
            .literal("reset", "clear")
            .argument(offlinePlayerArgument.copy())
            .handler { context ->
                val sender = context.sender as Player
                val offlinePlayer = context.getOptional(offlinePlayerArgument).orElse(null)

                if (offlinePlayer == null) {
                    val user = plugin.usersHandler.getUser(sender.uniqueId) ?: return@handler
                    user.clearJoinMessages()
                    sender.sendRichMessage(plugin.messagesConfig.joinMessageReset)
                } else {
                    val user = plugin.usersHandler.getUser(offlinePlayer.uniqueId)
                    if (user == null) {
                        sender.sendRichMessage("Could not find user ${offlinePlayer.name} in the database!")
                        return@handler
                    }
                    user.clearJoinMessages()
                    sender.sendMessage(plugin.messagesConfig.joinMessageResetOther.parse(mapOf("displayname" to user.playerName)))
                }
            }
        )
    }
}