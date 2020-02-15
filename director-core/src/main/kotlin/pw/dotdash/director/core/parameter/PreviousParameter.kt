@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter

/**
 * Consumes no tokens, and returns the previous value.
 *
 * @return The new value parameter
 */
fun <P> previous(): ValueParameter<Any?, P, P> = @Suppress("UNCHECKED_CAST") (PreviousParameter as ValueParameter<Any?, P, P>)

private object PreviousParameter : ValueParameter<Any?, Any?, Any?> {
    override fun parse(source: Any?, tokens: CommandTokens, previous: Any?): Any? = previous

    override fun getUsage(source: Any?, key: String): String = ""
}