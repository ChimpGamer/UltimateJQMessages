package nl.chimpgamer.ultimatejqmessages.paper.menus

import dev.dejvokep.boostedyaml.block.implementation.Section
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.configurations.MenuConfig
import nl.chimpgamer.ultimatejqmessages.paper.extensions.parse
import nl.chimpgamer.ultimatejqmessages.paper.models.MenuItem
import nl.chimpgamer.ultimatejqmessages.paper.utils.ItemUtils
import nl.chimpgamer.ultimatejqmessages.paper.utils.StringUtils
import nl.chimpgamer.ultimatejqmessages.paper.models.ConfigurableSound
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType

abstract class ConfigurableMenu(protected val plugin: UltimateJQMessagesPlugin, fileName: String) :
    MenuConfig(plugin, plugin.menusFolder.resolve(fileName)) {

    var menuTitle: String? = null
        get() = if (field == null) file.nameWithoutExtension else field
        set(value) {
            field = value ?: file.nameWithoutExtension
        }

    var menuSize: Int

    val menuItems = HashMap<String, MenuItem>()

    private var openingSound: ConfigurableSound? = null
    protected var closingSound: ConfigurableSound? = null

    lateinit var inventory: RyseInventory

    protected fun updateDisplayNameAndLore(
        itemStack: ItemStack,
        player: Player,
        tagResolver: TagResolver = TagResolver.empty()
    ): ItemStack {
        val clonedItemStacked = itemStack.clone()
        if (clonedItemStacked.hasItemMeta()) {
            clonedItemStacked.editMeta { meta ->
                val displayName = meta.displayName.parse(tagResolver)
                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                val lore = meta.lore?.map {
                    it.parse(tagResolver)
                        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                }
                meta.displayName(displayName)
                meta.lore(lore)

                if (meta is SkullMeta) {
                    if (!meta.hasOwner()) {
                        val pdc = meta.persistentDataContainer
                        pdc.get(ItemUtils.skullOwnerNamespacedKey, PersistentDataType.STRING)
                            ?.let { skullOwner ->
                                val finalSkullOwner =
                                    StringUtils.applyPlaceholders(skullOwner, player)
                                val offlinePlayer = Bukkit.getOfflinePlayerIfCached(finalSkullOwner)
                                meta.setOwningPlayer(offlinePlayer)
                            }
                    }
                }
            }
        }
        return clonedItemStacked
    }

    private fun loadItems() {
        menuItems.clear()
        val section = config.getSection("items")
        if (section != null) {
            for (key in section.keys) {
                val menuItem = loadItem(section, key.toString())
                if (menuItem != null) {
                    menuItems[key.toString()] = menuItem
                }
            }
        }
    }

    private fun loadItem(section: Section, name: String): MenuItem? {
        val itemSection = section.getSection(name)
        if (itemSection == null) {
            println("$name does not exist in the config")
            return null
        }
        val menuItem = MenuItem(name)
        menuItem.itemStack = ItemUtils.itemDataToItemStack(plugin, itemSection.getStringList("item"))
        if (itemSection.contains("position")) {
            menuItem.position = itemSection.getInt("position")
        }
        return menuItem
    }

    fun open(player: Player, page: Int = 1) {
        inventory.newInstance().open(player, page)
        openingSound?.play(player)
    }

    init {
        menuTitle = config.getString("title", file.nameWithoutExtension)
        menuSize = config.getInt("size", 54)
        if (menuSize < 18 || menuSize > 54) {
            menuSize = 54
        }

        val soundsSection = config.getSection("sounds")
        if (soundsSection != null) {
            if (soundsSection.contains("opening")) {
                openingSound =
                    ConfigurableSound.deserialize(soundsSection.getSection("opening").getStringRouteMappedValues(false))
            }
            if (soundsSection.contains("closing")) {
                closingSound =
                    ConfigurableSound.deserialize(soundsSection.getSection("closing").getStringRouteMappedValues(false))
            }
        }

        loadItems()
    }
}