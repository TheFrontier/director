@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.exception.ArgumentParseException
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter

@JvmOverloads
fun <S, P, V : Any> ValueParameter<S, P, V>.optional(default: (() -> V)? = null, weak: Boolean = false): ValueParameter<S, P, V?> =
    OptionalParameter(this, default, weak)

private data class OptionalParameter<S, P, V : Any>(
    val parameter: ValueParameter<S, P, V>,
    val default: (() -> V)?,
    val weak: Boolean
) : ValueParameter<S, P, V?> {

    override fun parse(source: S, tokens: CommandTokens, previous: P): V? {
        if (!tokens.hasNext()) return this.default?.invoke()

        val snapshot: CommandTokens.Snapshot = tokens.snapshot

        try {
            return this.parameter.parse(source, tokens, previous)
        } catch (e: ArgumentParseException) {
            if (this.weak || tokens.hasNext()) {
                tokens.snapshot = snapshot
                return this.default?.invoke()
            }
            throw e
        }
    }

    override fun getUsage(source: S, key: String): String =
        "[${this.parameter.getUsage(source, key)}]"
}