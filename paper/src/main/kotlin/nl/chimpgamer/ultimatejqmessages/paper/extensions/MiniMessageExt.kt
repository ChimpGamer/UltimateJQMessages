package nl.chimpgamer.ultimatetags.extensions

import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatetags.models.Tag

fun String.parse() = miniMessage().deserialize(this).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)

fun String.parse(tagResolver: TagResolver) = miniMessage().deserialize(this, tagResolver)

fun String.parse(replacements: Map<String, *>) = parse(replacements.toTagResolver())

fun String.isValid(): Boolean = miniMessage().deserializeOrNull(this) != null

fun Map<String, *>.toTagResolver(parsed: Boolean = false) = TagResolver.resolver(
    map { (key, value) ->
        if (value is ComponentLike) Placeholder.component(key, value)
        else if (parsed) Placeholder.parsed(key, value.toString())
        else Placeholder.unparsed(key, value.toString())
    }
)

internal fun getTagPlaceholders(tag: Tag): TagResolver = TagResolver.builder()
    .resolver(Placeholder.parsed("name", tag.name))
    .resolver(Placeholder.parsed("tag", tag.tag))
    .resolver(Placeholder.parsed("description", tag.description ?: ""))
    .resolver(Placeholder.parsed("server", tag.server ?: ""))
    .build()