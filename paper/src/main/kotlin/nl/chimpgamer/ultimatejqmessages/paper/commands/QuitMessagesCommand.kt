package nl.chimpgamer.ultimatejqmessages.paper.commands

import cloud.commandframework.CommandManager
import cloud.commandframework.arguments.standard.StringArgument
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.parse
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class JoinMessagesCommand(private val plugin: UltimateJQMessagesPlugin) {

    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val basePermission = "ultimatejqmessages.command.joinmessages"
        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermission)

        // Arguments
        val nameArgument = StringArgument.of<CommandSender>("name")
        val messageArgument = StringArgument.of<CommandSender>("message")

        commandManager.command(builder
            .senderType(Player::class.java)
            .handler { context ->
                val sender = context.sender as Player
                // Open menu...
            }
        )

        commandManager.command(builder
            .permission("$basePermission.create")
            .literal("create")
            .argument(nameArgument.copy())
            .argument(messageArgument.copy())
            .handler { context ->
                val sender = context.sender
                val messageName = context[nameArgument]
                val message = context[messageArgument]

                if (!message.equals("<displayname>", ignoreCase = true)) {
                    sender.sendRichMessage("")
                    return@handler
                }

                val joinQuitMessage = plugin.joinQuitMessagesHandler.createJoinQuitMessage(messageName, JoinQuitMessageType.JOIN, message)
                sender.sendRichMessage("Successfully created join message ${joinQuitMessage.name}")
                sender.sendRichMessage("<gray>${joinQuitMessage.message}")
            }
        )
    }
}