@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.exception.ArgumentParseException
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter

/**
 * Wraps the given [this], which will attempt to parse and, failing that,
 * the source will be returned if they are of type [V].
 *
 * @receiver The parameter to first attempt
 * @return The new value parameter
 */
inline fun <S, P, reified V> ValueParameter<S, P, V>.orSource(): ValueParameter<S, P, V> =
    object : ValueParameter<S, P, V> {
        override fun parse(source: S, tokens: CommandTokens, previous: P): V {
            val snapshot: CommandTokens.Snapshot = tokens.snapshot
            try {
                return this@orSource.parse(source, tokens, previous)
            } catch (e: ArgumentParseException) {
                if (source is V) {
                    tokens.snapshot = snapshot
                    return source
                } else {
                    throw e
                }
            }
        }

        override fun complete(source: S, tokens: CommandTokens, previous: P): List<String> =
            this@orSource.complete(source, tokens, previous)

        override fun getUsage(source: S, key: String): String =
            "[${this@orSource.getUsage(source, key)}]"
    }