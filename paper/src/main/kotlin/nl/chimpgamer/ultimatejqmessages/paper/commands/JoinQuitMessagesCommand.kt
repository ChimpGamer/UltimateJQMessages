package nl.chimpgamer.ultimatejqmessages.paper.commands

import cloud.commandframework.CommandManager
import cloud.commandframework.arguments.standard.StringArgument
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.parse
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class JoinQuitMessagesCommand(private val plugin: UltimateJQMessagesPlugin) {
    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val basePermission = "ultimatejqmessages.command.joinquitmessages"
        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermission)

        // Arguments
        val nameArgument = StringArgument.of<CommandSender>("name")
        val typeArgument = StringArgument.of<CommandSender>("type")
        val messageArgument = StringArgument.greedy<CommandSender>("message")

        commandManager.command(builder
            .literal("help")
            .permission("$basePermission.help")
            .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
            .handler { context ->
                plugin.cloudCommandManager.joinQuitMessagesHelp.queryCommands(
                    context.getOrDefault("query", ""),
                    context.sender
                )
            }
        )

        commandManager.command(builder
            .permission("$basePermission.create")
            .literal("create")
            .argument(nameArgument.copy())
            .argument(typeArgument.copy())
            .argument(messageArgument.copy())
            .handler { context ->
                val sender = context.sender
                val messageName = context[nameArgument]
                val type = context[typeArgument]
                val message = context[messageArgument]

                val joinQuitMessageType = runCatching { JoinQuitMessageType.valueOf(type.uppercase()) }.getOrElse {
                    sender.sendRichMessage("$type is not a valid type. Use JOIN or QUIT.")
                    return@handler
                }

                if (!message.contains("<displayname>", ignoreCase = true)) {
                    if (joinQuitMessageType === JoinQuitMessageType.JOIN) {
                        sender.sendRichMessage(plugin.messagesConfig.joinMessageCreateMissingPlaceholder)
                    } else {
                        sender.sendRichMessage(plugin.messagesConfig.quitMessageCreateMissingPlaceholder)
                    }
                    return@handler
                }

                val joinQuitMessage = plugin.joinQuitMessagesHandler.createJoinQuitMessage(messageName, joinQuitMessageType, message)
                sender.sendRichMessage("Successfully created ${joinQuitMessageType.name.lowercase()} message ${joinQuitMessage.name}")
                sender.sendRichMessage("<gray>${joinQuitMessage.message}")
            }
        )

        commandManager.command(builder
            .permission("$basePermission.delete")
            .literal("delete")
            .argument(nameArgument.copy())
            .handler { context ->
                val sender = context.sender
                val messageName = context[nameArgument]

                val joinQuitMessagesHandler = plugin.joinQuitMessagesHandler
                val joinQuitMessage = joinQuitMessagesHandler.getJoinQuitMessageByName(messageName)
                if (joinQuitMessage == null) {
                    sender.sendRichMessage("Join Quit message $messageName does not exist!")
                    return@handler
                }
                joinQuitMessagesHandler.deleteJoinQuitMessage(joinQuitMessage)
                sender.sendRichMessage("Successfully deleted ${joinQuitMessage.type.name.lowercase()} message ${joinQuitMessage.name}!")
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .permission("$basePermission.toggle")
            .literal("toggle")
            .handler { context ->
                val sender = context.sender as Player
                val user = plugin.usersHandler.getUser(sender.uniqueId) ?: return@handler

                val newState = !user.showJoinQuitMessages
                user.showJoinQuitMessages(newState)
                sender.sendMessage("Toggled join quit message <state>".parse(Formatter.booleanChoice("state", newState)))
            }
        )
    }
}