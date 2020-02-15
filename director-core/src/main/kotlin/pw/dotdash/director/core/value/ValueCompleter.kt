package pw.dotdash.director.core.value

import pw.dotdash.director.core.exception.ArgumentParseException
import pw.dotdash.director.core.lexer.CommandTokens

interface ValueCompleter<in S, in P> {

    @Throws(ArgumentParseException::class)
    fun complete(source: S, tokens: CommandTokens, previous: P): List<String>
}