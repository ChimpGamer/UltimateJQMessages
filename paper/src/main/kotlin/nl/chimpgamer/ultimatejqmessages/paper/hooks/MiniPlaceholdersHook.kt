package nl.chimpgamer.ultimatejqmessages.paper.hooks

import io.github.miniplaceholders.api.Expansion
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import org.bukkit.entity.Player

class MiniPlaceholdersHook(private val plugin: UltimateJQMessagesPlugin) {
    private val name = "MiniPlaceholders"
    private val isLoaded get() = plugin.server.pluginManager.isPluginEnabled(name)

    private lateinit var expansion: Expansion

    fun load() {
        if (!isLoaded) return
        val joinQuitMessagesHandler = plugin.joinQuitMessagesHandler
        expansion = Expansion.builder("ultimatejqmessages")
            .filter(Player::class.java)

            .globalPlaceholder("total_join_messages") { _, _ ->
                Tag.selfClosingInserting(Component.text(joinQuitMessagesHandler.getJoinMessages().size))
            }
            .globalPlaceholder("total_quit_messages") { _, _ ->
                Tag.selfClosingInserting(Component.text(joinQuitMessagesHandler.getQuitMessages().size))
            }
            .globalPlaceholder("total_messages") { _, _ ->
                Tag.selfClosingInserting(Component.text(joinQuitMessagesHandler.getAllMessages().size))
            }

            .audiencePlaceholder("show_join_quit_messages") { audience, _, _ ->
                audience as Player
                val user = plugin.usersHandler.getIfLoaded(audience.uniqueId) ?: return@audiencePlaceholder null
                return@audiencePlaceholder Tag.selfClosingInserting(Component.text(user.showJoinQuitMessages))
            }
            .audiencePlaceholder("has_join_message_selected") { audience, _, _ ->
                audience as Player
                val user = plugin.usersHandler.getIfLoaded(audience.uniqueId) ?: return@audiencePlaceholder null
                return@audiencePlaceholder Tag.selfClosingInserting(Component.text(user.joinMessage != null))
            }
            .audiencePlaceholder("has_quit_message_selected") { audience, _, _ ->
                audience as Player
                val user = plugin.usersHandler.getIfLoaded(audience.uniqueId) ?: return@audiencePlaceholder null
                return@audiencePlaceholder Tag.selfClosingInserting(Component.text(user.quitMessage != null))
            }
            .build()

        expansion.register()
    }

    fun unload() {
        if (this::expansion.isInitialized) {
            expansion.unregister()
        }
    }
}