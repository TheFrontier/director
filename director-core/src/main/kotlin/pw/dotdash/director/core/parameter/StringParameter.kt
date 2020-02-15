@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter

/**
 * Expect an argument to represent a string. Every provided argument will match.
 */
fun string(): ValueParameter<Any?, Any?, String> = StringParameter

private object StringParameter : ValueParameter<Any?, Any?, String> {
    override fun parse(source: Any?, tokens: CommandTokens, previous: Any?): String =
        tokens.next()
}

