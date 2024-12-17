package nl.chimpgamer.ultimatejqmessages.paper.events

import nl.chimpgamer.ultimatejqmessages.paper.models.User
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ToggleShowJoinQuitMessagesEvent(val user: User, val showJoinQuitMessages: Boolean) : Event(true) {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}