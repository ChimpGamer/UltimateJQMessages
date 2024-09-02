package nl.chimpgamer.ultimatejqmessages.paper.placeholders

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import org.bukkit.entity.Player

class InternalPlaceholders(private val plugin: UltimateJQMessagesPlugin) : PlaceholderHook() {

    override fun playerPlaceholders(player: Player): TagResolver {
        val tags = TagResolver.builder()
        val playerTags = listOf(
            Placeholder.component("player_name", player.name()),
            Placeholder.component("player_display_name", player.displayName()),
        )
        tags.resolvers(playerTags)
        val user = plugin.usersHandler.getIfLoaded(player.uniqueId)
        if (user != null) {
            val userTags = listOf(
                Placeholder.parsed("custom_join_message", user.customJoinMessage ?: ""),
                Placeholder.parsed("custom_quit_message", user.customQuitMessage ?: ""),
                Placeholder.parsed("join_message", user.joinMessage?.message ?: ""),
                Placeholder.parsed("join_message_name", user.joinMessage?.name ?: ""),
                Placeholder.parsed("quit_message", user.quitMessage?.message ?: ""),
                Placeholder.parsed("quit_message_name", user.quitMessage?.name ?: ""),
            )
            tags.resolvers(userTags)
        }

        return tags.build()
    }
}