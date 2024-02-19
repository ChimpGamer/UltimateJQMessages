package nl.chimpgamer.ultimatejqmessages.paper.menus

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.rysefoxx.inventory.plugin.content.IntelligentItem
import io.github.rysefoxx.inventory.plugin.content.InventoryContents
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider
import io.github.rysefoxx.inventory.plugin.events.RyseInventoryOpenEvent
import io.github.rysefoxx.inventory.plugin.other.EventCreator
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import io.github.rysefoxx.inventory.plugin.pagination.SlotIterator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.*
import nl.chimpgamer.ultimatejqmessages.paper.utils.Utils
import org.bukkit.entity.Player

class QuitMessageSelectorMenu(plugin: UltimateJQMessagesPlugin) :
    ConfigurableMenu(plugin, "quit_message_selector.yml") {

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
                    val user = usersHandler.getIfLoaded(player.uniqueId)
                    if (user == null) {
                        inventory.close(player)
                        return
                    }
                    val tagResolverBuilder = TagResolver.builder()
                        .resolver(Placeholder.parsed("page", currentPage.toString()))
                        .resolver(Placeholder.parsed("next_page", nextPage.toString()))
                        .resolver(Placeholder.parsed("previous_page", previousPage.toString()))
                        .resolver(Placeholder.parsed("custom_quit_message", user.customQuitMessage ?: ""))
                        .resolver(getDisplayNamePlaceholder(player))

                    val joinQuitMessagesHandler = plugin.joinQuitMessagesHandler

                    val lockedQuitMessageItem = menuItems["LockedQuitMessageItem"]
                    val unlockedQuitMessageItem = menuItems["UnlockedQuitMessageItem"]
                    val selectedQuitMessageItem = menuItems["SelectedQuitMessageItem"]

                    joinQuitMessagesHandler.getQuitMessages().forEach { quitMessage ->
                        val selected = user.quitMessage == quitMessage
                        val hasPermission = quitMessage.hasPermission(player)

                        val itemStack = if (!hasPermission) {
                            lockedQuitMessageItem?.itemStack
                        } else if (selected) {
                            selectedQuitMessageItem?.itemStack
                        } else {
                            unlockedQuitMessageItem?.itemStack
                        }
                        if (itemStack == null) return@forEach
                        tagResolverBuilder
                            .resolver(Placeholder.parsed("name", quitMessage.name))
                            .resolver(Placeholder.parsed("quit_message_name", quitMessage.name))
                            .resolver(Placeholder.parsed("quit_message", quitMessage.message))
                        val tagResolver = tagResolverBuilder.build()

                        val joinQuitMessageSelectItem = updateDisplayNameAndLore(itemStack, player, tagResolver)

                        pagination.addItem(IntelligentItem.of(joinQuitMessageSelectItem) {
                            plugin.launch {
                                if (!selected && hasPermission) {
                                    usersHandler.setQuitMessage(user, quitMessage)
                                    player.sendMessage(plugin.messagesConfig.quitMessageSet.parse(tagResolver))
                                    closeAndReopen(player, currentPage)
                                }
                            }
                        })
                    }

                    pagination.iterator(
                        SlotIterator.builder().startPosition(0).type(SlotIterator.SlotIteratorType.HORIZONTAL).build()
                    )

                    val tagResolver = tagResolverBuilder.build()
                    if (!pagination.isFirst) {
                        val previousPageItem = menuItems["PreviousPageItem"]?.itemStack
                        if (previousPageItem != null) {
                            contents[menuSize - 9] =
                                IntelligentItem.of(updateDisplayNameAndLore(previousPageItem, player, tagResolver)) {
                                    inventory.open(player, pagination.previous().page())
                                }
                        }
                    }

                    val customQuitMessageItem = menuItems["CustomQuitMessageItem"]?.itemStack
                    if (customQuitMessageItem != null) {
                        contents[menuSize - 7] =
                            IntelligentItem.of(updateDisplayNameAndLore(customQuitMessageItem, player, tagResolver)) {
                                inventory.close(player)
                                if (!player.hasPermission("ultimatejqmessages.customquitmessage")) {
                                    player.sendRichMessage(plugin.messagesConfig.noPermission)
                                    return@of
                                }
                                player.sendRichMessage(plugin.messagesConfig.quitMessageCreateCustomChat)

                                val playerInputBuilder = Utils.createChatInputBuilderBase(plugin, player)
                                    .isValidInput { _, input ->
                                        var valid = false
                                        if (input.contains("<displayname>", ignoreCase = true)) {
                                            val component = input.parseOrNull()
                                            if (component != null) {
                                                val maxLength = plugin.settingsConfig.quitMessagesCustomMaxLength
                                                val componentLength = component.length()
                                                if (componentLength > maxLength) {
                                                    player.sendRichMessage(plugin.messagesConfig.quitMessagesCreateCustomTooLong)
                                                } else {
                                                    valid = true
                                                }
                                            }
                                        }
                                        valid
                                    }
                                    .onInvalidInput { player, input ->
                                        player.sendMessage(plugin.messagesConfig.quitMessageCreateInvalidInput.parse(Placeholder.parsed("input", input)))
                                        false
                                    }
                                    .onFinish { player, input ->
                                        plugin.launch {
                                            player.sendActionBar(Component.empty())

                                            usersHandler.setCustomQuitMessage(user, input)
                                            val title = plugin.messagesConfig.quitMessageCreateCustomSetTitle.toTitle()
                                            player.showTitle(title)
                                            player.sendMessage(plugin.messagesConfig.quitMessageCreateCustomSetChat.parse(
                                                TagResolver.resolver(Placeholder.parsed("custom_quit_message", user.customQuitMessage ?: ""), getDisplayNamePlaceholder(player))
                                            ))
                                        }
                                    }

                                val playerInput = playerInputBuilder.build()
                                playerInput.start()
                                val title = plugin.messagesConfig.quitMessageCreateCustomTitle.toTitle(1L, 300L, 1L)
                                player.showTitle(title)
                            }
                    }

                    val closeMenuItem = menuItems["CloseMenuItem"]?.itemStack
                    if (closeMenuItem != null) {
                        contents[menuSize - 5] =
                            IntelligentItem.of(updateDisplayNameAndLore(closeMenuItem, player, tagResolver)) {
                                inventory.close(player)
                            }
                    }

                    val clearQuitMessageItem = menuItems["ClearQuitMessageItem"]?.itemStack
                    if (clearQuitMessageItem != null) {
                        contents[menuSize - 3] =
                            IntelligentItem.of(updateDisplayNameAndLore(clearQuitMessageItem, player, tagResolver)) {
                                plugin.launch {
                                    usersHandler.clearQuitMessages(user)
                                    player.sendRichMessage(plugin.messagesConfig.quitMessageReset)
                                    closeAndReopen(player, currentPage)
                                }
                            }
                    }

                    if (!pagination.isLast) {
                        val nextPageItem = menuItems["NextPageItem"]?.itemStack
                        if (nextPageItem != null) {
                            contents[menuSize - 1] =
                                IntelligentItem.of(updateDisplayNameAndLore(nextPageItem, player, tagResolver)) {
                                    inventory.open(player, pagination.next().page())
                                }
                        }
                    }
                }

                override fun close(player: Player, inventory: RyseInventory) {
                    closingSound?.play(player)
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

    private fun closeAndReopen(player: Player, page: Int = 1) {
        inventory.close(player)
        inventory.open(player, page)
    }

    init {
        buildInventory()
    }
}