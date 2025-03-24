package nl.chimpgamer.ultimatejqmessages.paper

import com.github.shynixn.mccoroutine.folia.*
import io.github.rysefoxx.inventory.plugin.pagination.InventoryManager
import kotlinx.coroutines.CoroutineStart
import nl.chimpgamer.ultimatejqmessages.paper.commands.CloudCommandManager
import nl.chimpgamer.ultimatejqmessages.paper.configurations.MessagesConfig
import nl.chimpgamer.ultimatejqmessages.paper.configurations.SettingsConfig
import nl.chimpgamer.ultimatejqmessages.paper.handlers.DataHandler
import nl.chimpgamer.ultimatejqmessages.paper.handlers.JoinQuitMessagesHandler
import nl.chimpgamer.ultimatejqmessages.paper.handlers.UsersHandler
import nl.chimpgamer.ultimatejqmessages.paper.listeners.PlayerConnectionListener
import nl.chimpgamer.ultimatejqmessages.paper.extensions.registerEvents
import nl.chimpgamer.ultimatejqmessages.paper.hooks.PluginHookManager
import nl.chimpgamer.ultimatejqmessages.paper.menus.JoinMessageSelectorMenu
import nl.chimpgamer.ultimatejqmessages.paper.menus.QuitMessageSelectorMenu
import nl.chimpgamer.ultimatejqmessages.paper.placeholders.InternalPlaceholders
import nl.chimpgamer.ultimatejqmessages.paper.placeholders.PlaceholderManager
import nl.chimpgamer.ultimatejqmessages.paper.extensions.registerSuspendingEvents
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext

class UltimateJQMessagesPlugin : JavaPlugin() {
    val menusFolder = File(dataFolder, "menus")
    val inventoryManager = InventoryManager(this)

    val settingsConfig = SettingsConfig(this)
    val messagesConfig = MessagesConfig(this)

    val dataHandler = DataHandler(this)
    val joinQuitMessagesHandler = JoinQuitMessagesHandler(this)
    val usersHandler = UsersHandler(this)

    val cloudCommandManager = CloudCommandManager(this)

    val pluginHookManager = PluginHookManager(this)
    val placeholderManager = PlaceholderManager()

    lateinit var joinMessageSelectorMenu: JoinMessageSelectorMenu
    lateinit var quitMessageSelectorMenu: QuitMessageSelectorMenu

    override fun onLoad() {
        // Make sure that the UltimateJQMessages folder exists.
        try {
            val dataFolderPath = dataFolder.toPath()
            if (!Files.isDirectory(dataFolderPath)) {
                Files.createDirectories(dataFolderPath)
            }
        } catch (ex: IOException) {
            logger.log(Level.SEVERE, "Unable to create plugin directory", ex)
        }
    }

    override fun onEnable() {
        val plugin = this
        val eventDispatcher = mapOf<Class<out Event>, (event: Event) -> CoroutineContext>(
            Pair(MCCoroutineExceptionEvent::class.java) {
                require(it is MCCoroutineExceptionEvent)
                plugin.globalRegionDispatcher
            },
            Pair(AsyncPlayerPreLoginEvent::class.java) {
                require(it is AsyncPlayerPreLoginEvent)
                asyncDispatcher
            },
            Pair(PlayerJoinEvent::class.java) {
                require(it is PlayerJoinEvent)
                entityDispatcher(it.player)
            },
            Pair(PlayerQuitEvent::class.java) {
                require(it is PlayerQuitEvent)
                entityDispatcher(it.player)
            },
        )

        inventoryManager.invoke()

        dataHandler.initialize()
        joinQuitMessagesHandler.load()

        joinMessageSelectorMenu = JoinMessageSelectorMenu(this)
        quitMessageSelectorMenu = QuitMessageSelectorMenu(this)

        cloudCommandManager.initialize()
        cloudCommandManager.loadCommands()

        pluginHookManager.load()

        placeholderManager.registerPlaceholder(InternalPlaceholders(this))

        registerSuspendingEvents(
            PlayerConnectionListener(this),
            eventDispatcher = eventDispatcher
        )

        registerEvents(
            pluginHookManager
        )
    }

    override fun onDisable() {
        closeMenus()
        pluginHookManager.unload()
        HandlerList.unregisterAll(this)

        if (dataHandler.isDatabaseInitialized) {
            dataHandler.close()
        }
    }

    suspend fun reload() {
        launch(globalRegionDispatcher, CoroutineStart.UNDISPATCHED) {
            closeMenus()
        }

        settingsConfig.config.reload()
        messagesConfig.config.reload()

        // Reload join quit messages
        joinQuitMessagesHandler.load()
        // Reload users
        usersHandler.reload()

        joinMessageSelectorMenu = JoinMessageSelectorMenu(this)
        quitMessageSelectorMenu = QuitMessageSelectorMenu(this)
    }

    private fun closeMenus() {
        server.onlinePlayers.forEach { player ->
            inventoryManager.getInventory(player.uniqueId).ifPresent { it.close(player) }
        }
    }
}