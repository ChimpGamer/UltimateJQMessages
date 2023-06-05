package nl.chimpgamer.ultimatejqmessages.paper.utils

import net.kyori.adventure.text.Component
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import org.bukkit.entity.Player

object Utils {

    fun createChatInputBuilderBase(
        plugin: UltimateJQMessagesPlugin,
        player: Player
    ): PlayerChatInput.PlayerChatInputBuilder<String> =
        PlayerChatInput.PlayerChatInputBuilder<String>(plugin, player)
            .onExpire {
                it.clearTitle()
                it.sendActionBar(Component.empty())
            }
            .onCancel {
                it.clearTitle()
                it.sendActionBar(Component.empty())
            }
            .setValue { _, s -> s }
            .expiresAfter(6000)
            .invalidInputMessage(null)
            .sendValueMessage(null)
            .onExpireMessage(null)
}