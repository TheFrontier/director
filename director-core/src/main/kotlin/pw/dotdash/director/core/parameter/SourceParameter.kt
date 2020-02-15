@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter

/**
 * Consumes no tokens, and returns the source.
 *
 * @return The new value parameter
 */
fun <S> source(): ValueParameter<S, Any?, S> = @Suppress("UNCHECKED_CAST") (SourceParameter as ValueParameter<S, Any?, S>)

private object SourceParameter : ValueParameter<Any?, Any?, Any?> {
    override fun parse(source: Any?, tokens: CommandTokens, previous: Any?): Any? = source

    override fun getUsage(source: Any?, key: String): String = ""
}