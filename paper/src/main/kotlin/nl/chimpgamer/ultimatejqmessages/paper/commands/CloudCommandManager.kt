package nl.chimpgamer.ultimatejqmessages.paper.commands

import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.exceptions.NoPermissionException
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler
import cloud.commandframework.minecraft.extras.MinecraftHelp
import cloud.commandframework.paper.PaperCommandManager
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.parse
import org.bukkit.command.CommandSender
import java.util.function.Function
import java.util.logging.Level

class CloudCommandManager(private val plugin: UltimateJQMessagesPlugin) {
    private lateinit var paperCommandManager: PaperCommandManager<CommandSender>
    lateinit var joinQuitMessagesHelp: MinecraftHelp<CommandSender>

    fun initialize() {
        val executionCoordinatorFunction = AsynchronousCommandExecutionCoordinator.builder<CommandSender>().build()

        try {
            paperCommandManager = PaperCommandManager(
                plugin,
                executionCoordinatorFunction,
                Function.identity(),
                Function.identity()
            )

            if (paperCommandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
                paperCommandManager.registerBrigadier()
                val brigadierManager = paperCommandManager.brigadierManager()
                brigadierManager?.setNativeNumberSuggestions(false)
            }
            if (paperCommandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
                paperCommandManager.registerAsynchronousCompletions()
            }

            MinecraftExceptionHandler<CommandSender>()
                .withArgumentParsingHandler()
                .withInvalidSenderHandler()
                .withInvalidSyntaxHandler()
                .withCommandExecutionHandler()
                .withHandler(MinecraftExceptionHandler.ExceptionType.NO_PERMISSION) { e ->
                    e as NoPermissionException
                    plugin.messagesConfig.noPermission.parse(Placeholder.parsed("missing_permission", e.missingPermission))
                }
                .apply(paperCommandManager) { it }

            joinQuitMessagesHelp = MinecraftHelp.createNative("/joinquitmessages help", paperCommandManager)
        } catch (ex: Exception) {
            plugin.logger.log(Level.SEVERE, "Failed to initialize the command manager", ex)
        }
    }

    fun loadCommands() {
        JoinMessagesCommand(plugin).registerCommands(paperCommandManager, "joinmessages", "joinmessage")
        QuitMessagesCommand(plugin).registerCommands(paperCommandManager, "quitmessages", "quitmessage")
        JoinQuitMessagesCommand(plugin).registerCommands(paperCommandManager, "joinquitmessages", "ultimatejoinquitmessages", "ultimatejqmessages", "ujqmessages", "ujqm")
    }
}