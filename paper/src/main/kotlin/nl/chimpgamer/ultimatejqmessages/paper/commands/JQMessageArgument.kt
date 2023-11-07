package nl.chimpgamer.ultimatejqmessages.paper.commands

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.arguments.CommandArgument
import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.arguments.parser.ArgumentParser
import cloud.commandframework.captions.CaptionVariable
import cloud.commandframework.context.CommandContext
import cloud.commandframework.exceptions.parsing.NoInputProvidedException
import cloud.commandframework.exceptions.parsing.ParserException
import io.leangen.geantyref.TypeToken
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.models.JoinQuitMessage
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.function.BiFunction

class JQMessageArgument private constructor(
    required: Boolean,
    name: String,
    suggestionsProvider: BiFunction<CommandContext<CommandSender>, String, List<String>>?,
    defaultDescription: ArgumentDescription,
    argumentPreprocessors: Collection<BiFunction<CommandContext<CommandSender>, Queue<String>, ArgumentParseResult<Boolean>>>
) : CommandArgument<CommandSender, JoinQuitMessage>(
    required,
    name,
    TagParser(),
    "",
    TypeToken.get(JoinQuitMessage::class.java),
    suggestionsProvider,
    defaultDescription,
    argumentPreprocessors
) {

    class Builder(name: String) : CommandArgument.Builder<CommandSender, JoinQuitMessage>(TypeToken.get(JoinQuitMessage::class.java), name) {
        override fun build(): CommandArgument<CommandSender, JoinQuitMessage> {
            return JQMessageArgument(
                this.isRequired,
                name,
                suggestionsProvider,
                defaultDescription,
                LinkedList()
            )
        }
    }

    class TagParser : ArgumentParser<CommandSender, JoinQuitMessage> {
        override fun parse(
            commandContext: CommandContext<CommandSender>,
            inputQueue: Queue<String>
        ): ArgumentParseResult<JoinQuitMessage> {
            val input = inputQueue.peek()
                ?: return ArgumentParseResult.failure(
                    NoInputProvidedException(
                        TagParser::class.java,
                        commandContext
                    )
                )
            val tag = JavaPlugin.getPlugin(UltimateJQMessagesPlugin::class.java)
                .joinQuitMessagesHandler.getJoinQuitMessageByName(input) ?: return ArgumentParseResult.failure(
                JQMessageParseException(
                    input,
                    commandContext
                )
            )
            inputQueue.remove()
            return ArgumentParseResult.success(tag)
        }

        override fun suggestions(
            commandContext: CommandContext<CommandSender>,
            input: String
        ): List<String> {
            return JavaPlugin.getPlugin(UltimateJQMessagesPlugin::class.java).joinQuitMessagesHandler.getAllMessages().map { it.name }
        }

        override fun isContextFree(): Boolean {
            return true
        }
    }

    class JQMessageParseException(
        input: String,
        context: CommandContext<*>
    ) : ParserException(
        TagParser::class.java,
        context,
        UltimateJQMessagesCaptionKeys.ARGUMENT_PARSE_FAILURE_JOIN_QUIT_MESSAGE,
        CaptionVariable.of("input", input)
    ) {
        companion object {
            private const val serialVersionUID = -2685136673577959929L
        }
    }

    companion object {
        /**
         * Create a new argument builder
         *
         * @param name Argument name
         * @return Constructed builder
        </C> */
        fun newBuilder(name: String): CommandArgument.Builder<CommandSender, JoinQuitMessage> {
            return Builder(name).withParser(
                TagParser()
            )
        }

        /**
         * Create a new required language argument
         *
         * @param name Argument name
         * @return Created argument
        </C> */
        fun of(name: String): CommandArgument<CommandSender, JoinQuitMessage> {
            return newBuilder(name).asRequired().build()
        }

        /**
         * Create a new optional language argument
         *
         * @param name Argument name
         * @return Created argument
        </C> */
        fun optional(name: String): CommandArgument<CommandSender, JoinQuitMessage> {
            return newBuilder(name).asOptional().build()
        }
    }
}