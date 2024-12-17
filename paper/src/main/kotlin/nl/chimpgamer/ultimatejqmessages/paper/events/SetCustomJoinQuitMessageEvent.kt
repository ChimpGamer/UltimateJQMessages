package nl.chimpgamer.ultimatejqmessages.paper.events

import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessageType
import nl.chimpgamer.ultimatejqmessages.paper.models.User
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class SetCustomJoinQuitMessageEvent(val user: User, val type: JoinQuitMessageType, val message: String?) : Event(true) {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}