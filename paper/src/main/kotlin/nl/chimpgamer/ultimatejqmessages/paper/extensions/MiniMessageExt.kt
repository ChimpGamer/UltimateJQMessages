package nl.chimpgamer.ultimatejqmessages.paper.extensions

import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatejqmessages.paper.models.User
import org.bukkit.entity.Player

fun String.parse() = miniMessage().deserialize(this).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)

fun String.parse(tagResolver: TagResolver) = miniMessage().deserialize(this, tagResolver)

fun String.parse(replacements: Map<String, *>) = parse(replacements.toTagResolver())

fun String.parseOrNull() = miniMessage().deserializeOrNull(this)

fun Map<String, *>.toTagResolver(parsed: Boolean = false) = TagResolver.resolver(
    map { (key, value) ->
        if (value is ComponentLike) Placeholder.component(key, value)
        else if (parsed) Placeholder.parsed(key, value.toString())
        else Placeholder.unparsed(key, value.toString())
    }
)

internal fun getDisplayNamePlaceholder(player: Player) = Placeholder.component("displayname", player.displayName())

internal fun getQuitMessagePlaceholders(player: Player, user: User) = TagResolver.builder()
    .resolver(getDisplayNamePlaceholder(player))

    .resolver(Placeholder.parsed("custom_quit_message", user.customQuitMessage ?: ""))
    .build()