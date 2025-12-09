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
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.*
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import nl.chimpgamer.ultimatejqmessages.paper.utils.Utils
import org.bukkit.entity.Player

class JoinMessageSelectorMenu(plugin: UltimateJQMessagesPlugin) :
    SelectorMenu(plugin, "join_message_selector.yml") {

    private fun buildInventory() {
        inventory = RyseInventory.builder()
            .provider(object : InventoryProvider {
                override fun init(player: Player, contents: InventoryContents) {
                    val user = setupPaginationAndValidateUser(player, contents) ?: return
                    val tagResolverBuilder = createBaseTagResolver(contents, player, JoinQuitMessageType.JOIN)
                    val pagination = contents.pagination()
                    val currentPage = pagination.page()

                    val usersHandler = plugin.usersHandler
                    val joinQuitMessagesHandler = plugin.joinQuitMessagesHandler

                    val lockedJoinMessageItem = menuItems["LockedJoinMessageItem"]
                    val unlockedJoinMessageItem = menuItems["UnlockedJoinMessageItem"]
                    val selectedJoinMessageItem = menuItems["SelectedJoinMessageItem"]

                    joinQuitMessagesHandler.getJoinMessagesSorted().forEach { joinMessage ->
                        val selected = user.joinMessage == joinMessage
                        val hasPermission = joinMessage.hasPermission(player)

                        val itemStack = getMessageItemStack(joinMessage, hasPermission, selected, lockedJoinMessageItem, unlockedJoinMessageItem, selectedJoinMessageItem)

                        if (itemStack == null) return@forEach
                        tagResolverBuilder
                            .resolver(Placeholder.parsed("name", joinMessage.name))
                            .resolver(Placeholder.parsed("join_message_name", joinMessage.name))
                            .resolver(Placeholder.parsed("join_message", joinMessage.message))
                        val tagResolver = tagResolverBuilder.build()

                        val joinQuitMessageSelectItem = updateDisplayNameAndLore(itemStack, player, tagResolver)

                        pagination.addItem(IntelligentItem.of(joinQuitMessageSelectItem) {
                            plugin.launch(plugin.asyncDispatcher) {
                                if (selected) {
                                    usersHandler.setJoinMessage(user, null)
                                    withContext(plugin.entityDispatcher(player)) {
                                        closeAndReopen(player, currentPage)
                                    }
                                } else if (hasPermission) {
                                    usersHandler.setJoinMessage(user, joinMessage)
                                    player.sendMessage(plugin.messagesConfig.joinMessageSet.parse(tagResolver))
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
                        val previousPageItem = getItem("PreviousPageItem")
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

                    val customJoinMessageItem = getItem("CustomJoinMessageItem")
                    if (customJoinMessageItem?.hasPermission(player) == true) {
                        customJoinMessageItem.itemStack?.let { itemStack ->
                            val position = if (customJoinMessageItem.position == -1) menuSize - 7 else customJoinMessageItem.position
                            contents[position] = IntelligentItem.of(updateDisplayNameAndLore(itemStack, player, tagResolver)) {
                                inventory.close(player)
                                if (!player.hasPermission("ultimatejqmessages.customjoinmessage")) {
                                    player.sendRichMessage(plugin.messagesConfig.noPermission)
                                    return@of
                                }
                                player.sendRichMessage(plugin.messagesConfig.joinMessageCreateCustomChat)

                                val playerInputBuilder = Utils.createChatInputBuilderBase(plugin, player)
                                    .isValidInput { _, input ->
                                        var valid = false

                                        if (input.contains("<displayname>", ignoreCase = true)) {
                                            val component = input.parseOrNull()
                                            if (component != null) {
                                                val maxLength = plugin.settingsConfig.joinMessagesCustomMaxLength
                                                val componentLength = component.length()
                                                if (componentLength > maxLength) {
                                                    player.sendRichMessage(plugin.messagesConfig.joinMessagesCreateCustomTooLong)
                                                } else {
                                                    valid = true
                                                }
                                            }
                                        }
                                        valid
                                    }
                                    .onInvalidInput { player, input ->
                                        player.sendMessage(
                                            plugin.messagesConfig.joinMessageCreateInvalidInput.parse(
                                                Placeholder.parsed("input", input)
                                            )
                                        )
                                        false
                                    }
                                    .onFinish { player, input ->
                                        plugin.launch(plugin.asyncDispatcher) {
                                            player.sendActionBar(Component.empty())

                                            usersHandler.setCustomJoinMessage(user, input)
                                            val title = plugin.messagesConfig.joinMessageCreateCustomSetTitle.toTitle()
                                            player.showTitle(title)
                                            player.sendMessage(
                                                plugin.messagesConfig.joinMessageCreateCustomSetChat.parse(Placeholder.parsed(
                                                    "custom_join_message",
                                                    user.customJoinMessage ?: ""
                                                ))
                                            )
                                        }
                                    }

                                val playerInput = playerInputBuilder.build()
                                playerInput.start()
                                val title = plugin.messagesConfig.joinMessageCreateCustomTitle.toTitle(1L, 300L, 1L)
                                player.showTitle(title)
                            }
                        }
                    }

                    setupCloseMenuItem(contents, player, tagResolver)

                    setupRandomToggleItem(contents, user, player, tagResolver)

                    val clearJoinMessageItem = getItem("ClearJoinMessageItem")
                    if (clearJoinMessageItem?.hasPermission(player) == true) {
                        clearJoinMessageItem.itemStack?.let { itemStack ->
                            val position = if (clearJoinMessageItem.position == -1) menuSize - 3 else clearJoinMessageItem.position
                            contents[position] = IntelligentItem.of(updateDisplayNameAndLore(itemStack, player, tagResolver)) {
                                plugin.launch(plugin.asyncDispatcher) {
                                    usersHandler.clearJoinMessages(user)
                                    player.sendRichMessage(plugin.messagesConfig.joinMessageReset)
                                    closeAndReopen(player, currentPage)
                                }
                            }
                        }
                    }

                    if (!pagination.isLast) {
                        val nextPageItem = getItem("NextPageItem")
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

    init {
        buildInventory()
    }
}