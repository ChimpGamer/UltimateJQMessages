package nl.chimpgamer.ultimatejqmessages.paper.commands

import cloud.commandframework.CommandManager
import cloud.commandframework.arguments.standard.StringArgument
import com.github.shynixn.mccoroutine.bukkit.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.parse
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessage
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import org.bukkit.command.CommandSender
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.nio.file.Files

class JoinQuitMessagesCommand(private val plugin: UltimateJQMessagesPlugin) {
    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val basePermission = "ultimatejqmessages.command.joinquitmessages"
        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermission)

        // Arguments
        val nameArgument = StringArgument.of<CommandSender>("name")
        val typeArgument = StringArgument.of<CommandSender>("type")
        val messageArgument = StringArgument.greedy<CommandSender>("message")

        val fileNameArgument = StringArgument.of<CommandSender>("file")

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
            .literal("reload")
            .permission("$basePermission.reload")
            .handler { context ->
                val sender = context.sender
                plugin.reload()
                sender.sendRichMessage("<green>Successfully reloaded configs!")
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
                plugin.launch {
                    val sender = context.sender as Player
                    val usersHandler = plugin.usersHandler
                    val user = plugin.usersHandler.getIfLoaded(sender.uniqueId) ?: return@launch

                    val newState = !user.showJoinQuitMessages
                    usersHandler.setShowJoinQuitMessages(user, newState)
                    sender.sendMessage(plugin.messagesConfig.joinQuitMessagesToggle.parse(Formatter.booleanChoice("state", newState)))
                }
            }
        )

        commandManager.command(builder
            .permission("$basePermission.export")
            .literal("export")
            .handler { context ->
                val sender = context.sender

                val exportsFolder = plugin.dataFolder.resolve("exports")
                if (!Files.isDirectory(exportsFolder.toPath())) {
                    Files.createDirectories(exportsFolder.toPath())
                }
                val exportFile = File(exportsFolder, "${System.currentTimeMillis()}_join_quit_messages.yml")
                val config = YamlConfiguration()
                plugin.joinQuitMessagesHandler.getAllMessages().forEach { joinQuitMessage ->
                    val section = config.createSection(joinQuitMessage.id.toString())
                    section.apply {
                        set("name", joinQuitMessage.name)
                        set("type", joinQuitMessage.type.toString())
                        set("message", joinQuitMessage.message)
                    }
                }
                try {
                    config.save(exportFile)
                    sender.sendRichMessage("<green>Successfully exported all join quit messages!")
                } catch (ex: IOException) {
                    sender.sendRichMessage("<red>Something went wrong while tying to save the export!<br>${ex.localizedMessage}")
                }
            }
        )

        commandManager.command(builder
            .permission("$basePermission.import")
            .literal("import")
            .argument(fileNameArgument)
            .handler { context ->
                val sender = context.sender
                val fileName = context[fileNameArgument]

                val exportsFolder = plugin.dataFolder.resolve("exports")
                if (!Files.isDirectory(exportsFolder.toPath())) {
                    Files.createDirectories(exportsFolder.toPath())
                }
                val exportFile = File(exportsFolder, fileName)
                if (!exportFile.exists()) {
                    sender.sendRichMessage("<red>$fileName does not exist!")
                    return@handler
                }
                val config = YamlConfiguration()
                try {
                    config.load(exportFile)
                } catch (ex: IOException) {
                    sender.sendRichMessage("<red>Cannot load file:<br>${ex.localizedMessage}")
                } catch (ex: InvalidConfigurationException) {
                    sender.sendRichMessage("<red>Cannot load file:<br>${ex.localizedMessage}")
                }
                val joinQuitMessages = HashSet<JoinQuitMessage>()
                for (key in config.getKeys(false)) {
                    val section = config.getConfigurationSection(key) ?: continue
                    val name = section.getString("name")!!
                    val type = JoinQuitMessageType.valueOf(section.getString("type")!!)
                    val message = section.getString("message")!!
                    joinQuitMessages.add(JoinQuitMessage(null, name, type, message))
                }


            }
        )
    }
}