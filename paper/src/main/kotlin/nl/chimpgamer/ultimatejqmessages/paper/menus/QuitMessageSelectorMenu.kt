package nl.chimpgamer.ultimatejqmessages.paper.menus

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import io.github.rysefoxx.inventory.plugin.content.IntelligentItem
import io.github.rysefoxx.inventory.plugin.content.InventoryContents
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider
import io.github.rysefoxx.inventory.plugin.events.RyseInventoryOpenEvent
import io.github.rysefoxx.inventory.plugin.other.EventCreator
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import io.github.rysefoxx.inventory.plugin.pagination.SlotIterator
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.*
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
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
                        .resolver(playerGlobalPlaceholders(player))
                        .resolver(getDisplayNamePlaceholder(player, JoinQuitMessageType.QUIT))

                    val joinQuitMessagesHandler = plugin.joinQuitMessagesHandler

                    val lockedQuitMessageItem = menuItems["LockedQuitMessageItem"]
                    val unlockedQuitMessageItem = menuItems["UnlockedQuitMessageItem"]
                    val selectedQuitMessageItem = menuItems["SelectedQuitMessageItem"]

                    joinQuitMessagesHandler.getQuitMessagesSorted().forEach { quitMessage ->
                        val selected = user.quitMessage == quitMessage
                        val hasPermission = quitMessage.hasPermission(player)

                        val itemStack = if (!hasPermission) {
                            menuItems["Locked_${quitMessage.name}"]?.itemStack ?: lockedQuitMessageItem?.itemStack
                        } else if (selected) {
                            menuItems["Selected_${quitMessage.name}"]?.itemStack ?: selectedQuitMessageItem?.itemStack
                        } else {
                            menuItems["Unlocked_${quitMessage.name}"]?.itemStack ?: unlockedQuitMessageItem?.itemStack
                        }

                        if (itemStack == null) return@forEach
                        tagResolverBuilder
                            .resolver(Placeholder.parsed("name", quitMessage.name))
                            .resolver(Placeholder.parsed("quit_message_name", quitMessage.name))
                            .resolver(Placeholder.parsed("quit_message", quitMessage.message))
                        val tagResolver = tagResolverBuilder.build()

                        val joinQuitMessageSelectItem = updateDisplayNameAndLore(itemStack, player, tagResolver)

                        pagination.addItem(IntelligentItem.of(joinQuitMessageSelectItem) {
                            plugin.launch(plugin.asyncDispatcher) {
                                if (selected) {
                                    usersHandler.setQuitMessage(user, null)
                                    withContext(plugin.entityDispatcher(player)) {
                                        closeAndReopen(player, currentPage)
                                    }
                                } else if (hasPermission) {
                                    usersHandler.setQuitMessage(user, quitMessage)
                                    player.sendMessage(plugin.messagesConfig.quitMessageSet.parse(tagResolver))
                                    withContext(plugin.entityDispatcher(player)) {
                                        closeAndReopen(player, currentPage)
                                    }
                                }
                            }
                        })
                    }

                    pagination.iterator(
                        SlotIterator.builder().startPosition(0).type(SlotIterator.SlotIteratorType.HORIZONTAL).build()
                    )

                    val tagResolver = tagResolverBuilder.build()
                    if (!pagination.isFirst) {
                        val previousPageItem = menuItems["PreviousPageItem"]
                        if (previousPageItem?.hasPermission(player) == true) {
                            previousPageItem.itemStack?.let { itemStack ->
                                val position = if (previousPageItem.position == -1) menuSize - 9 else previousPageItem.position
                                contents[position] =
                                    IntelligentItem.of(updateDisplayNameAndLore(itemStack, player, tagResolver)) {
                                        inventory.open(player, pagination.previous().page())
                                    }
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
                                        player.sendMessage(
                                            plugin.messagesConfig.quitMessageCreateInvalidInput.parse(
                                                Placeholder.parsed("input", input)
                                            )
                                        )
                                        false
                                    }
                                    .onFinish { player, input ->
                                        plugin.launch(plugin.asyncDispatcher) {
                                            player.sendActionBar(Component.empty())

                                            usersHandler.setCustomQuitMessage(user, input)
                                            val title = plugin.messagesConfig.quitMessageCreateCustomSetTitle.toTitle()
                                            player.showTitle(title)
                                            player.sendMessage(
                                                plugin.messagesConfig.quitMessageCreateCustomSetChat.parse(
                                                    Placeholder.parsed(
                                                        "custom_quit_message",
                                                        user.customQuitMessage ?: ""
                                                    )
                                                )
                                            )
                                        }
                                    }

                                val playerInput = playerInputBuilder.build()
                                playerInput.start()
                                val title = plugin.messagesConfig.quitMessageCreateCustomTitle.toTitle(1L, 300L, 1L)
                                player.showTitle(title)
                            }
                    }

                    val closeMenuItem = menuItems["CloseMenuItem"]
                    if (closeMenuItem?.hasPermission(player) == true) {
                        closeMenuItem.itemStack?.let { itemStack ->
                            val position = if (closeMenuItem.position == -1) menuSize - 5 else closeMenuItem.position
                            contents[position] = IntelligentItem.of(updateDisplayNameAndLore(itemStack, player, tagResolver)) {
                                inventory.close(player)
                            }
                        }
                    }

                    val randomJoinQuitMessagesToggleItem = menuItems["RandomJoinQuitMessagesToggle"]
                    if (randomJoinQuitMessagesToggleItem?.hasPermission(player) == true) {
                        randomJoinQuitMessagesToggleItem.itemStack?.let { itemStack ->
                            contents[randomJoinQuitMessagesToggleItem.position] =
                                IntelligentItem.of(updateDisplayNameAndLore(itemStack, player, tagResolver)) {
                                    plugin.launch(plugin.asyncDispatcher) {
                                        usersHandler.setRandomJoinQuitMessages(user, !user.randomJoinQuitMessages)
                                        player.sendMessage(plugin.messagesConfig.joinQuitMessagesRandomToggle.parse(player))
                                    }
                                }
                        }
                    }

                    val clearQuitMessageItem = menuItems["ClearQuitMessageItem"]
                    if (clearQuitMessageItem?.hasPermission(player) == true) {
                        clearQuitMessageItem.itemStack?.let { itemStack ->
                            val position = if (clearQuitMessageItem.position == -1) menuSize - 3 else clearQuitMessageItem.position
                            contents[position] =
                                IntelligentItem.of(updateDisplayNameAndLore(itemStack, player, tagResolver)) {
                                    plugin.launch(plugin.asyncDispatcher) {
                                        usersHandler.clearQuitMessages(user)
                                        player.sendMessage(plugin.messagesConfig.quitMessageReset)
                                        closeAndReopen(player, currentPage)
                                    }
                                }
                        }
                    }

                    if (!pagination.isLast) {
                        val nextPageItem = menuItems["NextPageItem"]
                        if (nextPageItem?.hasPermission(player) == true) {
                            nextPageItem.itemStack?.let { itemStack ->
                                val position = if (nextPageItem.position == -1) menuSize - 1 else nextPageItem.position
                                contents[position] =
                                    IntelligentItem.of(updateDisplayNameAndLore(itemStack, player, tagResolver)) {
                                        inventory.open(player, pagination.next().page())
                                    }
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