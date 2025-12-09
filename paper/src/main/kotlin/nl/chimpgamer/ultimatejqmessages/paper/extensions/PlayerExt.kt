package nl.chimpgamer.ultimatejqmessages.paper.extensions

import org.bukkit.entity.Player
import kotlin.collections.any
import kotlin.collections.filter

fun Player.isVanished() = this.getMetadata("vanished").filter { it.value() is Boolean }.any { it.asBoolean() }