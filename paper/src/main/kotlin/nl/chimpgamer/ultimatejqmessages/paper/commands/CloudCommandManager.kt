package nl.chimpgamer.ultimatejqmessages.paper.commands

import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import org.bukkit.command.CommandSender
import org.incendo.cloud.brigadier.BrigadierSetting
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.caption.CaptionProvider
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler
import org.incendo.cloud.minecraft.extras.MinecraftHelp
import org.incendo.cloud.minecraft.extras.caption.ComponentCaptionFormatter
import org.incendo.cloud.paper.LegacyPaperCommandManager
import java.util.logging.Level

class CloudCommandManager(private val plugin: UltimateJQMessagesPlugin) {
    private lateinit var paperCommandManager: LegacyPaperCommandManager<CommandSender>
    lateinit var joinQuitMessagesHelp: MinecraftHelp<CommandSender>

    fun initialize() {
        try {
            paperCommandManager = LegacyPaperCommandManager.createNative(
                plugin,
                ExecutionCoordinator.asyncCoordinator()
            )

            if (paperCommandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
                try {
                    paperCommandManager.registerBrigadier()
                    val brigadierManager = paperCommandManager.brigadierManager()
                    brigadierManager.setNativeNumberSuggestions(false)
                    brigadierManager.settings().set(BrigadierSetting.FORCE_EXECUTABLE, true)
                    plugin.logger.info("Initialized Brigadier support!")
                } catch (e: Exception) {
                    plugin.logger.warning("Failed to initialize Brigadier support: " + e.message)
                }
            } else if (paperCommandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
                paperCommandManager.registerAsynchronousCompletions()
            }

            paperCommandManager.captionRegistry().run {
                registerProvider(CaptionProvider.constantProvider(UltimateJQMessagesCaptionKeys.ARGUMENT_PARSE_FAILURE_JOIN_QUIT_MESSAGE, "The message '<input>' does not exist!"))
                registerProvider(CaptionProvider.constantProvider(StandardCaptionKeys.EXCEPTION_NO_PERMISSION, plugin.messagesConfig.noPermission))
            }

            MinecraftExceptionHandler.createNative<CommandSender>()
                .defaultHandlers()
                .captionFormatter(ComponentCaptionFormatter.miniMessage())
                .registerTo(paperCommandManager)

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