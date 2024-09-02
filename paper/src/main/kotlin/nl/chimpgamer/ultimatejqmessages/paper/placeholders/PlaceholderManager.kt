package nl.chimpgamer.ultimatejqmessages.paper.placeholders

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatejqmessages.paper.extensions.applyIfNotEmpty
import org.bukkit.entity.Player

class PlaceholderManager {
    private val placeholderHooks = HashSet<PlaceholderHook>()

    fun registerPlaceholder(placeholderHook: PlaceholderHook): Boolean {
        return placeholderHooks.add(placeholderHook)
    }

    fun globalPlaceholders(): TagResolver {
        val builder = TagResolver.builder()
        placeholderHooks.forEach {
            builder.applyIfNotEmpty(it.globalPlaceholders())
        }
        return builder.build()
    }

    fun playerPlaceholders(player: Player): TagResolver {
        val builder = TagResolver.builder()
        placeholderHooks.forEach {
            builder.applyIfNotEmpty(it.playerPlaceholders(player))
        }
        return builder.build()
    }

    fun playerGlobalPlaceholders(player: Player): TagResolver {
        val builder = TagResolver.builder()
        placeholderHooks.forEach {
            builder.applyIfNotEmpty(it.globalPlaceholders())
            builder.applyIfNotEmpty(it.playerPlaceholders(player))
        }
        return builder.build()
    }
}