package nl.chimpgamer.ultimatejqmessages.paper.commands

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.CommandManager
import cloud.commandframework.arguments.flags.CommandFlag
import cloud.commandframework.arguments.standard.IntegerArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.bukkit.parsers.PlayerArgument
import cloud.commandframework.kotlin.coroutines.extension.suspendingHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.feature.pagination.Pagination
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.parse
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessage
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import org.bukkit.command.CommandSender
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
        val filePathArgument = StringArgument.of<CommandSender>("path")
        val playerArgument = PlayerArgument.of<CommandSender>("player")

        val joinQuitMessageArgument = JQMessageArgument.of("message_name")

        val overwriteFlag = CommandFlag.builder("overwrite").withDescription { "Overwrite existing join quit messages" }.build()

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
            .suspendingHandler { context ->
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
            .suspendingHandler { context ->
                val sender = context.sender

                val joinQuitMessage = context[joinQuitMessageArgument]
                val newMessage = context[messageArgument]

                val joinQuitMessagesHandler = plugin.joinQuitMessagesHandler

                joinQuitMessagesHandler.setMessage(joinQuitMessage, newMessage)
                sender.sendMessage("You changed the message of the ${joinQuitMessage.name} join quit message to:")
                sender.sendMessage(newMessage)
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
            .suspendingHandler { context ->
                val sender = context.sender

                val joinQuitMessage = context[joinQuitMessageArgument]
                val newPermission = context[permissionArgument]

                val joinQuitMessagesHandler = plugin.joinQuitMessagesHandler

                joinQuitMessagesHandler.setPermission(joinQuitMessage, newPermission)
                sender.sendMessage("You changed the permission of the ${joinQuitMessage.name} join quit message to:")
                sender.sendMessage(newPermission)
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .permission("$basePermission.toggle")
            .literal("toggle")
            .suspendingHandler { context ->
                val sender = context.sender as Player
                val usersHandler = plugin.usersHandler
                val user = usersHandler.getIfLoaded(sender.uniqueId) ?: return@suspendingHandler

                val newState = !user.showJoinQuitMessages
                usersHandler.setShowJoinQuitMessages(user, newState)
                sender.sendMessage(plugin.messagesConfig.joinQuitMessagesToggle.parse(Formatter.booleanChoice("state", newState)))
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .permission("$basePermission.customjoinmessage")
            .literal("customjoinmessage")
            .argument(playerArgument.copy())
            .argument(messageArgument.copy())
            .suspendingHandler { context ->
                val sender = context.sender as Player
                val player = context[playerArgument]
                val message = context[messageArgument]
                val usersHandler = plugin.usersHandler
                val user = usersHandler.getIfLoaded(player.uniqueId) ?: return@suspendingHandler
                usersHandler.setCustomJoinMessage(user, message)
                sender.sendRichMessage("<green>Successfully <gray>set custom join message for <yellow>${player.name}")
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .permission("$basePermission.customquitmessage")
            .literal("customquitmessage")
            .argument(playerArgument.copy())
            .argument(messageArgument.copy())
            .suspendingHandler { context ->
                val sender = context.sender as Player
                val player = context[playerArgument]
                val message = context[messageArgument]
                val usersHandler = plugin.usersHandler
                val user = usersHandler.getIfLoaded(player.uniqueId) ?: return@suspendingHandler
                usersHandler.setCustomQuitMessage(user, message)
                sender.sendRichMessage("<green>Successfully <gray>set custom quit message for <yellow>${player.name}")
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
            .argument(filePathArgument.copy())
            .flag(overwriteFlag)
            .handler { context ->
                val sender = context.sender

                val filePath = context[filePathArgument]
                val overwrite = context.flags().contains(overwriteFlag)

                val file = plugin.dataFolder.resolve(filePath)
                if (!file.isFile || file.extension != "yml") {
                    sender.sendRichMessage("<red>That is not a valid yml file!")
                    return@handler
                }

                val config = YamlConfiguration()
                runCatching { config.load(file) }.onFailure { sender.sendRichMessage("<red>Could not load file. Check the console for more details!"); it.printStackTrace() }

                var i = 0
                for (id in config.getKeys(false)) {
                    val section = config.getConfigurationSection(id) ?: continue
                    val jqName = section.getString("name")!!
                    val type = JoinQuitMessageType.valueOf(section.getString("type")!!)
                    val message = section.getString("message")!!
                    val permission = section.getString("permission")

                    if (!overwrite && plugin.joinQuitMessagesHandler.getJoinQuitMessageByName(jqName) != null) {
                        plugin.logger.info("[Import]: Skipped $jqName($id) tag. Name is already in use!")
                        continue
                    }

                    if (overwrite) {
                        plugin.joinQuitMessagesHandler.insertOrReplace(JoinQuitMessage(null, jqName, type, message, permission))
                    } else {
                        plugin.joinQuitMessagesHandler.createJoinQuitMessage(jqName, type, message, permission)
                    }
                    i++
                }

                sender.sendMessage("Imported $i join quit messages from ${file.name}!")
            }
        )
    }
}