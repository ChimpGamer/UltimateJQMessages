package nl.chimpgamer.ultimatejqmessages.paper

import io.github.rysefoxx.inventory.plugin.pagination.InventoryManager
import nl.chimpgamer.ultimatejqmessages.paper.commands.CloudCommandManager
import nl.chimpgamer.ultimatejqmessages.paper.configurations.MessagesConfig
import nl.chimpgamer.ultimatejqmessages.paper.configurations.SettingsConfig
import nl.chimpgamer.ultimatejqmessages.paper.handlers.DataHandler
import nl.chimpgamer.ultimatejqmessages.paper.handlers.JoinQuitMessagesHandler
import nl.chimpgamer.ultimatejqmessages.paper.handlers.UsersHandler
import nl.chimpgamer.ultimatejqmessages.paper.listeners.PlayerConnectionListener
import nl.chimpgamer.ultimatejqmessages.paper.extensions.registerEvents
import nl.chimpgamer.ultimatejqmessages.paper.extensions.runAsync
import nl.chimpgamer.ultimatejqmessages.paper.hooks.PluginHookManager
import nl.chimpgamer.ultimatejqmessages.paper.menus.JoinMessageSelectorMenu
import nl.chimpgamer.ultimatejqmessages.paper.menus.QuitMessageSelectorMenu
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

    private val pluginHookManager = PluginHookManager(this)

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
        runAsync {
            joinQuitMessagesHandler.load()
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