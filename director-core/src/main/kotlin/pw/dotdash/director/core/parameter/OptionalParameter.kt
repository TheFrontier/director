@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.exception.ArgumentParseException
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter

@JvmOverloads
fun <S, P : HList<P>, V : Any> ValueParameter<S, P, V>.optional(weak: Boolean = false): ValueParameter<S, P, V?> =
    OptionalParameter(this, weak)

private data class OptionalParameter<S, P : HList<P>, V : Any>(
    val parameter: ValueParameter<S, P, V>,
    val weak: Boolean
) : ValueParameter<S, P, V?> {

    override fun parse(source: S, tokens: CommandTokens, previous: P): V? {
        if (!tokens.hasNext()) return null

        val snapshot: CommandTokens.Snapshot = tokens.snapshot

        try {
            return this.parameter.parse(source, tokens, previous)
        } catch (e: ArgumentParseException) {
            if (this.weak || tokens.hasNext()) {
                tokens.snapshot = snapshot
                return null
            }
            throw e
        }
    }

    override fun complete(source: S, tokens: CommandTokens, previous: P): List<String> =
        parameter.complete(source, tokens, previous)

    override fun getUsage(source: S, key: String): String =
        "[${this.parameter.getUsage(source, key)}]"
}

@JvmOverloads
fun <S, P : HList<P>, V : Any> ValueParameter<S, P, V>.optionalOrElse(weak: Boolean = false, orElse: () -> V): ValueParameter<S, P, V> =
    OptionalOrElseParameter(this, weak, orElse)

private data class OptionalOrElseParameter<S, P : HList<P>, V : Any>(
    val parameter: ValueParameter<S, P, V>,
    val weak: Boolean,
    val default: (() -> V)
) : ValueParameter<S, P, V> {

    override fun parse(source: S, tokens: CommandTokens, previous: P): V {
        if (!tokens.hasNext()) return this.default.invoke()

        val snapshot: CommandTokens.Snapshot = tokens.snapshot

        try {
            return this.parameter.parse(source, tokens, previous)
        } catch (e: ArgumentParseException) {
            if (this.weak || tokens.hasNext()) {
                tokens.snapshot = snapshot
                return this.default.invoke()
            }
            throw e
        }
    }

    override fun complete(source: S, tokens: CommandTokens, previous: P): List<String> =
        parameter.complete(source, tokens, previous)

    override fun getUsage(source: S, key: String): String =
        "[${this.parameter.getUsage(source, key)}]"
}