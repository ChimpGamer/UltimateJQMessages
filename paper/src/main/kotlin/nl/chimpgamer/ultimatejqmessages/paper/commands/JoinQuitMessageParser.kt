package nl.chimpgamer.ultimatejqmessages.paper.commands

import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessage
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.caption.CaptionVariable
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import org.incendo.cloud.suggestion.Suggestion
import java.util.Objects

class JoinQuitMessageParser<C> : ArgumentParser<C, JoinQuitMessage>, BlockingSuggestionProvider<C> {
    companion object {
        fun <C> joinQuitMessageParser(): ParserDescriptor<C, JoinQuitMessage> {
            return ParserDescriptor.of(JoinQuitMessageParser(), JoinQuitMessage::class.java)
        }

        fun <C> joinQuitMessageComponent(): CommandComponent.Builder<C, JoinQuitMessage> {
            return CommandComponent.builder<C, JoinQuitMessage>().parser(joinQuitMessageParser())
        }
    }

    override fun parse(
        commandContext: CommandContext<C & Any>,
        commandInput: CommandInput
    ): ArgumentParseResult<JoinQuitMessage> {
        val input = commandInput.readString()
        val plugin = JavaPlugin.getPlugin(UltimateJQMessagesPlugin::class.java)
        val warp = plugin.joinQuitMessagesHandler.getJoinQuitMessageByName(input) ?: return ArgumentParseResult.failure(
            JoinQuitMessageParseException(input, commandContext)
        )
        return ArgumentParseResult.success(warp)
    }

    override fun suggestions(context: CommandContext<C>, input: CommandInput): Iterable<Suggestion> {
        val plugin = JavaPlugin.getPlugin(UltimateJQMessagesPlugin::class.java)
        return plugin.joinQuitMessagesHandler.getAllMessages().map { Suggestion.suggestion(it.name) }.toList()
    }

    class JoinQuitMessageParseException(private val input: String, context: CommandContext<*>) : ParserException(
        JoinQuitMessageParser::class.java,
        context,
        UltimateJQMessagesCaptionKeys.ARGUMENT_PARSE_FAILURE_JOIN_QUIT_MESSAGE,
        CaptionVariable.of("input", input)
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            } else if (other != null && this.javaClass == other.javaClass) {
                val that = other as JoinQuitMessageParseException
                return this.input == that.input
            } else {
                return false
            }
        }

        override fun hashCode(): Int {
            return Objects.hash(*arrayOf(this.input))
        }
    }
}
