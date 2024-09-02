package nl.chimpgamer.ultimatejqmessages.paper

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.launch
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
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.logging.Level

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
        inventoryManager.invoke()

        dataHandler.initialize()
        joinQuitMessagesHandler.load()

        joinMessageSelectorMenu = JoinMessageSelectorMenu(this)
        quitMessageSelectorMenu = QuitMessageSelectorMenu(this)

        cloudCommandManager.initialize()
        cloudCommandManager.loadCommands()

        pluginHookManager.load()

        placeholderManager.registerPlaceholder(InternalPlaceholders(this))

        registerEvents(
            PlayerConnectionListener(this),
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

    fun reload() {
        closeMenus()

        settingsConfig.config.reload()
        messagesConfig.config.reload()

        // Reload join quit messages
        launch(asyncDispatcher, CoroutineStart.UNDISPATCHED) {
            joinQuitMessagesHandler.load()
            usersHandler.reload()
        }

        joinMessageSelectorMenu = JoinMessageSelectorMenu(this)
        quitMessageSelectorMenu = QuitMessageSelectorMenu(this)
    }

    private fun closeMenus() {
        server.onlinePlayers.forEach { player ->
            inventoryManager.getInventory(player.uniqueId).ifPresent { it.close(player) }
        }
    }
}