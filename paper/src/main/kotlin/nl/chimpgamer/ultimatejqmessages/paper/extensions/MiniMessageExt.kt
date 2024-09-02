package nl.chimpgamer.ultimatejqmessages.paper.extensions

import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

fun String.parse() =
    miniMessage().deserialize(this).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)

fun String.parse(tagResolver: TagResolver) = miniMessage().deserialize(this, tagResolver)

fun String.parse(replacements: Map<String, *>) = parse(replacements.toTagResolver())

fun String.parse(player: Player?) = parse(
    TagResolver.resolver(
        if (player != null) playerGlobalPlaceholders(player) else globalPlaceholders(),
    )
)

fun String.parse(player: Player, joinQuitMessageType: JoinQuitMessageType) = parse(
    TagResolver.resolver(
        playerGlobalPlaceholders(player),
        getDisplayNamePlaceholder(player, joinQuitMessageType)
    )
)

fun String.parseOrNull() = miniMessage().deserializeOrNull(this)

fun Map<String, *>.toTagResolver(parsed: Boolean = false) = TagResolver.resolver(
    map { (key, value) ->
        if (value is ComponentLike) Placeholder.component(key, value)
        else if (parsed) Placeholder.parsed(key, value.toString())
        else Placeholder.unparsed(key, value.toString())
    }
)

fun TagResolver.isNotEmpty() = this != TagResolver.empty()

fun TagResolver.Builder.applyIfNotEmpty(resolver: TagResolver) {
    if (resolver.isNotEmpty()) {
        resolver(resolver)
    }
}

internal fun getDisplayNamePlaceholder(player: Player, joinQuitMessageType: JoinQuitMessageType) = Placeholder.component("displayname", ultimateJoinQuitMessagePlugin.settingsConfig.displayNameFormat(joinQuitMessageType).parse(player))

fun globalPlaceholders(): TagResolver =
    ultimateJoinQuitMessagePlugin.placeholderManager.globalPlaceholders()

fun playerPlaceholders(player: Player): TagResolver =
    ultimateJoinQuitMessagePlugin.placeholderManager.playerPlaceholders(player)

fun playerGlobalPlaceholders(player: Player): TagResolver = ultimateJoinQuitMessagePlugin.placeholderManager.playerGlobalPlaceholders(player)

private val ultimateJoinQuitMessagePlugin by lazy { JavaPlugin.getPlugin(UltimateJQMessagesPlugin::class.java) }