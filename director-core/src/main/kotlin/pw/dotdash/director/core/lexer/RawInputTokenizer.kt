package pw.dotdash.director.core.lexer

object RawInputTokenizer : InputTokenizer {

    override fun tokenize(arguments: String, lenient: Boolean): Sequence<Token> =
        sequenceOf(Token(arguments, 0, arguments.length))
}