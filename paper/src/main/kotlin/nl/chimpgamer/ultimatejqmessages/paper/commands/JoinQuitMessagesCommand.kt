package nl.chimpgamer.ultimatejqmessages.paper.commands

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.CommandManager
import cloud.commandframework.arguments.standard.IntegerArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.parsers.PlayerArgument
import com.github.shynixn.mccoroutine.bukkit.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.feature.pagination.Pagination
import net.kyori.adventure.text.format.NamedTextColor
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
    private val paginationBuilder = Pagination.builder()
        .width(53)
        .resultsPerPage(10)
        .renderer(object : Pagination.Renderer {
            override fun renderEmpty(): Component {
                return "<gray>There are no entries!".parse()
            }
        })

    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val basePermission = "ultimatejqmessages.command.joinquitmessages"
        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermission)

        // Arguments
        val nameArgument = StringArgument.of<CommandSender>("name")
        val typeArgument = StringArgument.of<CommandSender>("type")
        val messageArgument = StringArgument.greedy<CommandSender>("message")
        val permissionArgument = StringArgument.optional<CommandSender>("permission")

        val pageArgument = IntegerArgument.optional<CommandSender>("page")
        val fileNameArgument = StringArgument.of<CommandSender>("file")
        val playerArgument = PlayerArgument.of<CommandSender>("player")

        val joinQuitMessageArgument = JQMessageArgument.of("message_name")

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
            .argument(permissionArgument.copy())
            .handler { context ->
                val sender = context.sender
                val messageName = context[nameArgument]
                val type = context[typeArgument]
                val message = context[messageArgument]
                val permission = context.getOrDefault(permissionArgument, null)

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

                val joinQuitMessage = plugin.joinQuitMessagesHandler.createJoinQuitMessage(messageName, joinQuitMessageType, message, permission)
                sender.sendRichMessage("Successfully created ${joinQuitMessageType.name.lowercase()} message ${joinQuitMessage.name}")
                sender.sendRichMessage("<gray>${joinQuitMessage.message}")
            }
        )

        commandManager.command(builder
            .permission("$basePermission.delete")
            .literal("delete")
            .argument(joinQuitMessageArgument.copy())
            .handler { context ->
                val sender = context.sender
                val joinQuitMessage = context[joinQuitMessageArgument]

                plugin.joinQuitMessagesHandler.deleteJoinQuitMessage(joinQuitMessage)
                sender.sendRichMessage("Successfully deleted ${joinQuitMessage.type.name.lowercase()} message ${joinQuitMessage.name}!")
            }
        )

        commandManager.command(builder
            .literal(
                "setmessage",
                ArgumentDescription.of("Redefine the message of a existing join quit message")
            )
            .permission("$basePermission.setmessage")
            .argument(joinQuitMessageArgument.copy())
            .argument(messageArgument.copy())
            .handler { context ->
                val sender = context.sender

                val joinQuitMessage = context[joinQuitMessageArgument]
                val newMessage = context[messageArgument]

                val joinQuitMessagesHandler = plugin.joinQuitMessagesHandler

                plugin.launch {
                    joinQuitMessagesHandler.setMessage(joinQuitMessage, newMessage)
                    sender.sendMessage("You changed the message of the ${joinQuitMessage.name} join quit message to:")
                    sender.sendMessage(newMessage)
                }
            }
        )

        commandManager.command(builder
            .literal(
                "setpermission",
                ArgumentDescription.of("Redefine the permission of a existing join quit message")
            )
            .permission("$basePermission.setpermission")
            .argument(joinQuitMessageArgument.copy())
            .argument(permissionArgument.copy())
            .handler { context ->
                val sender = context.sender

                val joinQuitMessage = context[joinQuitMessageArgument]
                val newPermission = context[permissionArgument]

                val joinQuitMessagesHandler = plugin.joinQuitMessagesHandler

                plugin.launch {
                    joinQuitMessagesHandler.setPermission(joinQuitMessage, newPermission)
                    sender.sendMessage("You changed the permission of the ${joinQuitMessage.name} join quit message to:")
                    sender.sendMessage(newPermission)
                }
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
                    val user = usersHandler.getIfLoaded(sender.uniqueId) ?: return@launch

                    val newState = !user.showJoinQuitMessages
                    usersHandler.setShowJoinQuitMessages(user, newState)
                    sender.sendMessage(plugin.messagesConfig.joinQuitMessagesToggle.parse(Formatter.booleanChoice("state", newState)))
                }
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .permission("$basePermission.customjoinmessage")
            .literal("customjoinmessage")
            .argument(playerArgument.copy())
            .argument(messageArgument.copy())
            .handler { context ->
                plugin.launch {
                    val sender = context.sender as Player
                    val player = context[playerArgument]
                    val message = context[messageArgument]
                    val usersHandler = plugin.usersHandler
                    val user = usersHandler.getIfLoaded(player.uniqueId) ?: return@launch
                    usersHandler.setCustomJoinMessage(user, message)
                    sender.sendRichMessage("<green>Successfully <gray>set custom join message for <yellow>${player.name}")
                }
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .permission("$basePermission.customquitmessage")
            .literal("customquitmessage")
            .argument(playerArgument.copy())
            .argument(messageArgument.copy())
            .handler { context ->
                plugin.launch {
                    val sender = context.sender as Player
                    val player = context[playerArgument]
                    val message = context[messageArgument]
                    val usersHandler = plugin.usersHandler
                    val user = usersHandler.getIfLoaded(player.uniqueId) ?: return@launch
                    usersHandler.setCustomQuitMessage(user, message)
                    sender.sendRichMessage("<green>Successfully <gray>set custom quit message for <yellow>${player.name}")
                }
            }
        )

        commandManager.command(builder
            .permission("$basePermission.list")
            .literal("list")
            .handler { context ->
                val sender = context.sender
                val page = context.getOptional(pageArgument).orElse(1)

                val rows = ArrayList<Component>()
                plugin.joinQuitMessagesHandler.getAllMessages().sortedByDescending { it.id }.forEach { joinQuitMessage ->
                    rows.add("<dark_gray>» <gray>ID: <red>${joinQuitMessage.id} <gray>Name: <red>${joinQuitMessage.name} <gray>Type: <red>${joinQuitMessage.type} <gray>Message: ${joinQuitMessage.message} <gray>Permission: ${joinQuitMessage.permission}".parse())
                }
                val render = paginationBuilder.build(
                    Component.text(
                        "Join Quit Messages",
                        NamedTextColor.WHITE
                    ), { value: Component?, index: Int ->
                        listOf(
                            if (value == null) Component.text("${index + 1}. ").color(NamedTextColor.GREEN)
                                .content("ERR?")
                                .color(NamedTextColor.RED) else
                                Component.text("${index + 1}. ")
                                    .color(NamedTextColor.GREEN)
                                    .append(value)
                        )
                    }, { otherPage -> "/joinquitmessages list $otherPage" })
                    .render(rows, page)
                render.forEach(sender::sendMessage)
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
                        set("permission", joinQuitMessage.permission)
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
                    val permission = section.getString("permission")
                    joinQuitMessages.add(JoinQuitMessage(null, name, type, message, permission))
                }


            }
        )
    }
}