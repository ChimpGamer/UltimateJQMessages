package nl.chimpgamer.ultimatejqmessages.paper.commands

import org.incendo.cloud.caption.Caption
import java.util.LinkedList

object UltimateJQMessagesCaptionKeys {
    private val RECOGNIZED_CAPTIONS: MutableCollection<Caption> = LinkedList()

    val ARGUMENT_PARSE_FAILURE_JOIN_QUIT_MESSAGE = of("argument.parse.failure.joinquitmessage")

    private fun of(key: String): Caption {
        val caption = Caption.of(key)
        RECOGNIZED_CAPTIONS.add(caption)
        return caption
    }
}