package nl.chimpgamer.ultimatejqmessages.paper.placeholders

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

abstract class PlaceholderHook {
    open fun globalPlaceholders() = TagResolver.empty()

    open fun playerPlaceholders(player: Player) = TagResolver.empty()
}