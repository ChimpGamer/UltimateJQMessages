package nl.chimpgamer.ultimatetags.extensions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

private val legacyComponentSerializer = LegacyComponentSerializer.builder().character('&').hexColors().build()

fun Component.toLegacy() = legacyComponentSerializer.serialize(this)