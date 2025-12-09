package nl.chimpgamer.ultimatejqmessages.paper.menus

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import io.github.rysefoxx.inventory.plugin.content.IntelligentItem
import io.github.rysefoxx.inventory.plugin.content.InventoryContents
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.extensions.getDisplayNamePlaceholder
import nl.chimpgamer.ultimatejqmessages.paper.extensions.parse
import nl.chimpgamer.ultimatejqmessages.paper.extensions.playerGlobalPlaceholders
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessage
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import nl.chimpgamer.ultimatejqmessages.paper.models.MenuItem
import nl.chimpgamer.ultimatejqmessages.paper.models.User
import org.bukkit.entity.Player

abstract class SelectorMenu(plugin: UltimateJQMessagesPlugin, fileName: String) : ConfigurableMenu(plugin, fileName) {

    protected fun setupPaginationAndValidateUser(player: Player, contents: InventoryContents): User? {
        val pagination = contents.pagination()
        pagination.itemsPerPage = menuSize - 9

        val user = plugin.usersHandler.getIfLoaded(player.uniqueId)
        if (user == null) {
            inventory.close(player)
            return null
        }
        return user
    }

    protected fun createBaseTagResolver(contents: InventoryContents, player: Player, messageType: JoinQuitMessageType): TagResolver.Builder {
        val pagination = contents.pagination()
        val currentPage = pagination.page()
        val nextPage = pagination.next().page()
        val previousPage = pagination.previous().page()

        return TagResolver.builder()
            .resolver(Placeholder.parsed("page", currentPage.toString()))
            .resolver(Placeholder.parsed("next_page", nextPage.toString()))
            .resolver(Placeholder.parsed("previous_page", previousPage.toString()))
            .resolver(playerGlobalPlaceholders(player))
            .resolver(getDisplayNamePlaceholder(player, messageType))
    }

    protected fun getMessageItemStack(
        joinQuitMessage: JoinQuitMessage,
        hasPermission: Boolean,
        selected: Boolean,
        lockedJMessageItem: MenuItem?,
        unlockedMessageItem: MenuItem?,
        selectedMessageItem: MenuItem?
    ) = when {
        !hasPermission -> getItem("Locked_${joinQuitMessage.name}")?.itemStack ?: lockedJMessageItem?.itemStack
        selected -> getItem("Selected_${joinQuitMessage.name}")?.itemStack ?: selectedMessageItem?.itemStack
        else -> getItem("Unlocked_${joinQuitMessage.name}")?.itemStack ?: unlockedMessageItem?.itemStack
    }

    protected fun setupCloseMenuItem(contents: InventoryContents, player: Player, tagResolver: TagResolver) {
        val closeMenuItem = getItem("CloseMenuItem")
        if (closeMenuItem?.hasPermission(player) == true) {
            closeMenuItem.itemStack?.let { itemStack ->
                val position = if (closeMenuItem.position == -1) menuSize - 5 else closeMenuItem.position
                contents[position] = IntelligentItem.of(updateDisplayNameAndLore(itemStack, player, tagResolver)) {
                    inventory.close(player)
                }
            }
        }
    }

    protected fun setupRandomToggleItem(contents: InventoryContents, user: User, player: Player, tagResolver: TagResolver) {
        val randomJoinQuitMessagesToggleItem = getItem("RandomJoinQuitMessagesToggle")
        if (randomJoinQuitMessagesToggleItem?.hasPermission(player) == true) {
            randomJoinQuitMessagesToggleItem.itemStack?.let { itemStack ->
                contents[randomJoinQuitMessagesToggleItem.position] = IntelligentItem.of(updateDisplayNameAndLore(itemStack, player, tagResolver)) {
                    plugin.launch(plugin.asyncDispatcher) {
                        plugin.usersHandler.setRandomJoinQuitMessages(user, !user.randomJoinQuitMessages)
                        player.sendMessage(plugin.messagesConfig.joinQuitMessagesRandomToggle.parse(player))
                    }
                }
            }
        }
    }

    protected fun closeAndReopen(player: Player, page: Int = 1) {
        inventory.close(player)
        inventory.open(player, page)
    }
}