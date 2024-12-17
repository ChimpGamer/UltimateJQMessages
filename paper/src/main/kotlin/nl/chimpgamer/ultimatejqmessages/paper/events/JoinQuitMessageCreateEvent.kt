package nl.chimpgamer.ultimatejqmessages.paper.events

import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessage
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class JoinQuitMessageCreateEvent(val joinQuitMessage: JoinQuitMessage) : Event(true) {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}