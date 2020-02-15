package pw.dotdash.director.sponge.parameter

import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.text.selector.Selector
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.parameter.PatternMatchingParameter
import kotlin.reflect.KClass

/**
 * Abstract parameter that matches values based on a [Selector].
 */
abstract class SelectorParameter<in Previous, out Value : Any>(private val valueType: Class<out Value>) :
    PatternMatchingParameter<CommandSource, Previous, Value>() {

    constructor(valueType: KClass<out Value>) : this(valueType.java)

    override fun parse(source: CommandSource, tokens: CommandTokens, previous: Previous): Iterable<Value> {
        val token: String = tokens.peek()

        if (token.startsWith('@')) {
            try {
                return Selector.parse(tokens.next()).resolve(source).filterIsInstance(this.valueType)
            } catch (e: IllegalArgumentException) {
                throw tokens.createError(e.message ?: "An unknown error occurred while trying to parse the selector.")
            }
        }

        return super.parse(source, tokens, previous)
    }

    override fun complete(source: CommandSource, tokens: CommandTokens, previous: Previous): List<String> {
        val peekToken: String? = tokens.peekIfPresent()
        val choices: MutableList<String>? = peekToken?.let(Selector::complete)

        return if (choices.isNullOrEmpty()) super.complete(source, tokens, previous) else choices
    }
}