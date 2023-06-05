package nl.chimpgamer.ultimatejqmessages.paper.menus

import dev.dejvokep.boostedyaml.block.implementation.Section
import io.github.rysefoxx.inventory.plugin.content.IntelligentItem
import io.github.rysefoxx.inventory.plugin.content.InventoryContents
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider
import io.github.rysefoxx.inventory.plugin.events.RyseInventoryOpenEvent
import io.github.rysefoxx.inventory.plugin.other.EventCreator
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.configurations.MenuConfig
import nl.chimpgamer.ultimatejqmessages.paper.extensions.parse
import nl.chimpgamer.ultimatejqmessages.paper.models.MenuItem
import nl.chimpgamer.ultimatejqmessages.paper.utils.ItemUtils
import nl.chimpgamer.ultimatejqmessages.paper.utils.StringUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType

class MessageSelectorMenu(private val plugin: UltimateJQMessagesPlugin, fileName: String) :
    MenuConfig(plugin, plugin.menusFolder.resolve(fileName)) {
    var menuTitle: String? = null
        get() = if (field == null) file.nameWithoutExtension else field
        set(value) {
            field = value ?: file.nameWithoutExtension
        }

    private var menuSize: Int

    lateinit var inventory: RyseInventory

    val menuItems = HashMap<String, MenuItem>()

    private fun buildInventory() {
        inventory = RyseInventory.builder()
            .provider(object : InventoryProvider {
                override fun init(player: Player, contents: InventoryContents) {
                    val pagination = contents.pagination()
                    val currentPage = pagination.page()
                    val nextPage = pagination.next().page()
                    val previousPage = pagination.previous().page()

                    pagination.itemsPerPage = menuSize - 9

                    val usersHandler = plugin.usersHandler
                    val user = usersHandler.getUser(player.uniqueId)
                    if (user == null) {
                        inventory.close(player)
                        return
                    }
                    val joinQuitMessagesHandler = plugin.joinQuitMessagesHandler

                    val lockedJoinQuitMessageItem = menuItems["LockedJoinQuitMessageItem"]
                    val unlockedJoinQuitMessageItem = menuItems["UnlockedJoinQuitMessageItem"]
                    val selectedJoinQuitMessageItem = menuItems["SelectedJoinQuitMessageItem"]

                    joinQuitMessagesHandler.getJoinMessages().forEach { joinMessage ->
                        val selected = user.joinMessage == joinMessage
                        val hasPermission = player.hasPermission("ultimatejqmessages.access.${joinMessage.name}")

                        val itemStack = if (!hasPermission) {
                            lockedJoinQuitMessageItem?.itemStack
                        } else if (selected) {
                            selectedJoinQuitMessageItem?.itemStack
                        } else {
                            unlockedJoinQuitMessageItem?.itemStack
                        }
                        if (itemStack == null) return@forEach

                        val joinQuitMessageSelectItem = updateDisplayNameAndLore(itemStack, player)

                        pagination.addItem(IntelligentItem.of(joinQuitMessageSelectItem) {
                            if (!selected && hasPermission) {
                                user.setJoinMessage(joinMessage)
                                player.sendRichMessage(plugin.messagesConfig.joinMessageSet)
                                contents.reload()
                            }
                        })
                    }
                }
            })
            .listener(EventCreator(RyseInventoryOpenEvent::class.java) {
                plugin.inventoryManager.getContents(it.player.uniqueId).ifPresent { contents ->
                    val pagination = contents.pagination()
                    val contentPlaceholders = mapOf(
                        "page" to pagination.page(),
                        "maxpage" to pagination.lastPage()
                    )
                    contents.updateTitle(menuTitle.toString().parse(contentPlaceholders))
                }
            })
            .disableUpdateTask()
            .title(menuTitle.toString().parse())
            .size(menuSize)
            .build(plugin)
    }

    private fun updateDisplayNameAndLore(
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
        val section = config.getSection("Menu")
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
        menuItem.itemStack = ItemUtils.itemDataToItemStack(plugin, itemSection.getStringList("ItemData"))
        if (itemSection.contains("Position")) {
            menuItem.position = itemSection.getInt("Position")
        }
        return menuItem
    }

    init {
        menuTitle = config.getString("MenuTitle", file.nameWithoutExtension)
        menuSize = config.getInt("MenuSize", 54)
        if (menuSize < 18 || menuSize > 54) {
            menuSize = 54
        }

        loadItems()
    }
}