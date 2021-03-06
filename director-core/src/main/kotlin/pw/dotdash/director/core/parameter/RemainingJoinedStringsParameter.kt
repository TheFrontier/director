@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter

/**
 * Expect an argument to represent one or more strings,
 * which are combined into a single, space-separated string.
 */
fun remainingJoinedStrings(): ValueParameter<Any?, HList<*>, String> = RemainingJoinedStringsParameter

private object RemainingJoinedStringsParameter : ValueParameter<Any?, HList<*>, String> {
    override fun parse(source: Any?, tokens: CommandTokens, previous: HList<*>): String {
        val result = StringBuilder(tokens.next())
        while (tokens.hasNext()) {
            result.append(' ').append(tokens.next())
        }
        return result.toString()
    }
}