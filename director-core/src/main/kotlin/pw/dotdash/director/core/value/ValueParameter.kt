package pw.dotdash.director.core.value

import pw.dotdash.director.core.Parameter
import pw.dotdash.director.core.lexer.CommandTokens

interface ValueParameter<in S, in P, out V> : ValueParser<S, P, V>, ValueCompleter<S, P>, ValueUsage<S> {

    override fun complete(source: S, tokens: CommandTokens, previous: P): List<String> = emptyList()

    override fun getUsage(source: S, key: String): String = key

    infix fun key(key: String): Parameter<S, P, V> = Parameter(key, this)

    operator fun invoke(key: String): Parameter<S, P, V> = Parameter(key, this)
}