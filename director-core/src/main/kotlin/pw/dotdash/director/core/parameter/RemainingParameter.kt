@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.exception.ArgumentParseException
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter
import java.util.ArrayList

/**
 * Consumes all remaining tokens to output a [List] of [V]s.
 *
 * Acceptable Inputs
 * - All remaining values that successfully parse with [this].
 *
 * @receiver The parameter to repeat
 * @return The new value parameter
 */
fun <S, P : HList<P>, V> ValueParameter<S, P, V>.remaining(): ValueParameter<S, P, List<V>> =
    RemainingParameter(this)

private data class RemainingParameter<S, P : HList<P>, V>(val parameter: ValueParameter<S, P, V>) : ValueParameter<S, P, List<V>> {
    override fun parse(source: S, tokens: CommandTokens, previous: P): List<V> {
        val result = ArrayList<V>()
        while (tokens.hasNext()) {
            result += this.parameter.parse(source, tokens, previous)
        }
        return result
    }

    override fun complete(source: S, tokens: CommandTokens, previous: P): List<String> {
        while (tokens.hasNext()) {
            val snapshot: CommandTokens.Snapshot = tokens.snapshot
            try {
                this.parameter.parse(source, tokens, previous)
            } catch (e: ArgumentParseException) {
                tokens.snapshot = snapshot
                return this.parameter.complete(source, tokens, previous)
            }
        }
        return emptyList()
    }

    override fun getUsage(source: S, key: String): String = "<$key...>"
}