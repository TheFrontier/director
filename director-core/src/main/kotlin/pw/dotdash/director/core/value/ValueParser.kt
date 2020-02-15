package pw.dotdash.director.core.value

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.exception.ArgumentParseException
import pw.dotdash.director.core.lexer.CommandTokens

interface ValueParser<in S, in P : HList<@UnsafeVariance P>, out V> {

    @Throws(ArgumentParseException::class)
    fun parse(source: S, tokens: CommandTokens, previous: P): V
}