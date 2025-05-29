package nl.chimpgamer.ultimatejqmessages.paper.models

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class MenuItem(
    val name: String,
    var itemStack: ItemStack? = null,
    var position: Int = -1,
    var permission: String? = null,
    var message: String? = null
) {
    fun hasPermission(player: Player): Boolean = permission == null || player.hasPermission(permission!!)
}