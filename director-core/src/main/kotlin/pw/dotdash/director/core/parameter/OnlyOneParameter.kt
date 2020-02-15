@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter

/**
 * Consumes all tokens from the given [parameter][this], requiring only one value to parse, and returning this value.
 *
 * @receiver The parameter to require only one value
 * @return The new value parameter
 */
fun <S, P, V> ValueParameter<S, P, Iterable<V>>.onlyOne(): ValueParameter<S, P, V> =
    OnlyOneParameter(this)

private data class OnlyOneParameter<S, P, V>(val parameter: ValueParameter<S, P, Iterable<V>>) : ValueParameter<S, P, V> {
    override fun parse(source: S, tokens: CommandTokens, previous: P): V =
        this.parameter.parse(source, tokens, previous).singleOrNull()
            ?: throw tokens.createError("Multiple values found. You must select only one.")

    override fun complete(source: S, tokens: CommandTokens, previous: P): List<String> =
        this.parameter.complete(source, tokens, previous)

    override fun getUsage(source: S, key: String): String = "<$key>"
}