package nl.chimpgamer.ultimatejqmessages.paper.commands

import cloud.commandframework.CommandManager
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument
import com.github.shynixn.mccoroutine.bukkit.launch
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.parse
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class QuitMessagesCommand(private val plugin: UltimateJQMessagesPlugin) {

    fun registerCommands(commandManager: CommandManager<CommandSender>, name: String, vararg aliases: String) {
        val basePermission = "ultimatejqmessages.command.quitmessages"
        val builder = commandManager.commandBuilder(name, *aliases)
            .permission(basePermission)

        // Arguments
        val offlinePlayerArgument = OfflinePlayerArgument.optional<CommandSender>("player")

        commandManager.command(builder
            .senderType(Player::class.java)
            .handler { context ->
                val sender = context.sender as Player
                // Open menu...
                plugin.quitMessageSelectorMenu.open(sender)
            }
        )

        commandManager.command(builder
            .senderType(Player::class.java)
            .permission("$basePermission.reset")
            .literal("reset", "clear")
            .argument(offlinePlayerArgument.copy())
            .handler { context ->
                plugin.launch {
                    val sender = context.sender as Player
                    val offlinePlayer = context.getOptional(offlinePlayerArgument).orElse(null)

                    val usersHandler = plugin.usersHandler
                    if (offlinePlayer == null) {
                        val user = usersHandler.getIfLoaded(sender.uniqueId) ?: return@launch
                        usersHandler.clearQuitMessages(user)
                        sender.sendRichMessage(plugin.messagesConfig.quitMessageReset)
                    } else {
                        val user = usersHandler.getIfLoaded(offlinePlayer.uniqueId)
                        if (user == null) {
                            sender.sendRichMessage("Could not find user ${offlinePlayer.name} in the database!")
                            return@launch
                        }
                        usersHandler.clearQuitMessages(user)
                        sender.sendMessage(plugin.messagesConfig.quitMessageResetOther.parse(mapOf("displayname" to user.playerName)))
                    }
                }
            }
        )
    }
}