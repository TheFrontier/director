@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.exception.ArgumentParseException
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter
import java.util.ArrayList

infix fun <S, P, V> ValueParameter<S, P, V>.repeated(count: Int): ValueParameter<S, P, List<V>> =
    RepeatedParameter(this, count)

private data class RepeatedParameter<S, P, V>(val parameter: ValueParameter<S, P, V>, val count: Int) : ValueParameter<S, P, List<V>> {
    init {
        require(this.count > 0) { "count must be a positive, non-zero number" }
    }

    override fun parse(source: S, tokens: CommandTokens, previous: P): List<V> {
        val values = ArrayList<V>(this.count)
        repeat(this.count) {
            values += this.parameter.parse(source, tokens, previous)
        }
        return values
    }

    override fun complete(source: S, tokens: CommandTokens, previous: P): List<String> {
        repeat(this.count) {
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

    override fun getUsage(source: S, key: String): String = "${this.count}*<$key>"
}