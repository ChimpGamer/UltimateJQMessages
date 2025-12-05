package nl.chimpgamer.ultimatejqmessages.paper.hooks

import io.github.miniplaceholders.api.Expansion
import io.github.miniplaceholders.api.MiniPlaceholders
import io.github.miniplaceholders.kotlin.audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.placeholders.PlaceholderHook
import org.bukkit.entity.Player

class MiniPlaceholdersHook(plugin: UltimateJQMessagesPlugin) : PluginHook(plugin, "MiniPlaceholders") {

    private lateinit var expansion: Expansion

    override fun load() {
        if (!canHook()) return
        plugin.placeholderManager.registerPlaceholder(MiniPlaceholderHook())
        val joinQuitMessagesHandler = plugin.joinQuitMessagesHandler
        expansion = Expansion.builder("ultimatejqmessages")

            .globalPlaceholder("total_join_messages") { _, _ ->
                Tag.selfClosingInserting(Component.text(joinQuitMessagesHandler.getJoinMessages().size))
            }
            .globalPlaceholder("total_quit_messages") { _, _ ->
                Tag.selfClosingInserting(Component.text(joinQuitMessagesHandler.getQuitMessages().size))
            }
            .globalPlaceholder("total_messages") { _, _ ->
                Tag.selfClosingInserting(Component.text(joinQuitMessagesHandler.getAllMessages().size))
            }

            .audience<Player>("show_join_quit_messages") { audience, _, _ ->
                val user = plugin.usersHandler.getIfLoaded(audience.uniqueId) ?: return@audience null
                Tag.selfClosingInserting(Component.text(user.showJoinQuitMessages))
            }
            .audience<Player>("has_join_message_selected") { audience, _, _ ->
                val user = plugin.usersHandler.getIfLoaded(audience.uniqueId) ?: return@audience null
                Tag.selfClosingInserting(Component.text(user.joinMessage != null))
            }
            .audience<Player>("has_quit_message_selected") { audience, _, _ ->
                val user = plugin.usersHandler.getIfLoaded(audience.uniqueId) ?: return@audience null
                Tag.selfClosingInserting(Component.text(user.quitMessage != null))
            }
            .audience<Player>("join_messages_unlocked") { audience, _, _ ->
                Tag.selfClosingInserting(Component.text(joinQuitMessagesHandler.getQuitMessages().count { it.hasPermission(audience) }))
            }
            .audience<Player>("quit_messages_unlocked") { audience, _, _ ->
                Tag.selfClosingInserting(Component.text(joinQuitMessagesHandler.getQuitMessages().count { it.hasPermission(audience) }))
            }
            .build()

        expansion.register()
    }

    override fun unload() {
        if (this::expansion.isInitialized && expansion.registered()) {
            expansion.unregister()
        }
    }

    internal class MiniPlaceholderHook : PlaceholderHook() {
        override fun globalPlaceholders(): TagResolver {
            return MiniPlaceholders.globalPlaceholders()
        }

        override fun playerPlaceholders(player: Player): TagResolver {
            return MiniPlaceholders.audiencePlaceholders()
        }
    }
}