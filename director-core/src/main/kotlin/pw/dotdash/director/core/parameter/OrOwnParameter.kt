package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.exception.ArgumentParseException
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter

/**
 * Wraps the given [this], which will attempt to parse and, failing that,
 * [compute] will be called to find an alternative value based on the source.
 *
 * @receiver The parameter to first attempt
 * @param compute The lambda to call if the parameter fails
 * @return The new value parameter
 */
fun <S, P, V : Any> ValueParameter<S, P, V>.orOwn(compute: (S) -> V?): ValueParameter<S, P, V> =
    OrOwnParameter(this, compute)

private class OrOwnParameter<S, P, V>(val parameter: ValueParameter<S, P, V>, val compute: (S) -> V?) : ValueParameter<S, P, V> {
    override fun parse(source: S, tokens: CommandTokens, previous: P): V {
        val snapshot: CommandTokens.Snapshot = tokens.snapshot
        return try {
            this.parameter.parse(source, tokens, previous)
        } catch (e: ArgumentParseException) {
            val computed: V? = this.compute(source)
            if (computed != null) {
                tokens.snapshot = snapshot
                computed
            } else {
                throw e
            }
        }
    }

    override fun complete(source: S, tokens: CommandTokens, previous: P): List<String> =
        this.parameter.complete(source, tokens, previous)

    override fun getUsage(source: S, key: String): String =
        "[${this.parameter.getUsage(source, key)}]"
}