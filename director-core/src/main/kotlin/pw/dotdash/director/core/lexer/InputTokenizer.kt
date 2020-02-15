package pw.dotdash.director.core.lexer

import pw.dotdash.director.core.exception.ArgumentParseException

interface InputTokenizer {

    @Throws(ArgumentParseException::class)
    fun tokenize(arguments: String, lenient: Boolean): Sequence<Token>
}