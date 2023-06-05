package nl.chimpgamer.ultimatejqmessages.paper.models

import org.bukkit.inventory.ItemStack

data class MenuItem(
    val name: String,
    var itemStack: ItemStack? = null,
    var position: Int = -1,
)