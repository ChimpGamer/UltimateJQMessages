package nl.chimpgamer.ultimatejqmessages.paper.extensions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentIteratorType
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.title.Title
import java.time.Duration

private val legacyComponentSerializer = LegacyComponentSerializer.builder().character('&').hexColors().build()

fun Component.toLegacy() = legacyComponentSerializer.serialize(this)

fun Component.toPlainText() = PlainTextComponentSerializer.plainText().serialize(this)

fun Component.length() = iterable(ComponentIteratorType.DEPTH_FIRST)
    .filterIsInstance<TextComponent>()
    .sumOf { it.content().length }

fun String.toTitle(): Title {
    val title: Component
    var subTitle: Component = Component.empty()
    if (this.contains("<br>")) {
        val titleParts = this.split("<br>")
        title = titleParts[0].parse()
        subTitle = titleParts[1].parse()
    } else {
        title = this.parse()
    }
    return Title.title(title, subTitle)
}

fun String.toTitle(fadeIn: Long, stay: Long, fadeOut: Long): Title {
    val title: Component
    var subTitle: Component = Component.empty()
    if (this.contains("<br>")) {
        val titleParts = this.split("<br>")
        title = titleParts[0].parse()
        subTitle = titleParts[1].parse()
    } else {
        title = this.parse()
    }
    return Title.title(title, subTitle, Title.Times.times(Duration.ofSeconds(fadeIn), Duration.ofSeconds(stay), Duration.ofSeconds(fadeOut)))
}