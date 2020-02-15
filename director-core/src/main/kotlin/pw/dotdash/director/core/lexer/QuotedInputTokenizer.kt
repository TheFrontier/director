package pw.dotdash.director.core.lexer

import pw.dotdash.director.core.exception.ArgumentParseException

class QuotedInputTokenizer @JvmOverloads constructor(
    private val forceLenient: Boolean,
    private val trimTrailingSpace: Boolean = false
) : InputTokenizer {
    companion object {
        val DEFAULT = QuotedInputTokenizer(false)

        private const val BACKSLASH: Int = '\\'.toInt()
        private const val SINGLE_QUOTE: Int = '\''.toInt()
        private const val DOUBLE_QUOTE: Int = '"'.toInt()
    }

    override fun tokenize(arguments: String, lenient: Boolean): Sequence<Token> {
        if (arguments.isEmpty()) return emptySequence()

        return sequence {
            val state = TokenState(arguments, lenient)

            if (trimTrailingSpace) state.skipWhitespace()

            while (state.hasMore) {
                if (!trimTrailingSpace) state.skipWhitespace()

                val startIndex = state.index + 1
                val arg = state.nextArg()
                yield(Token(arg, startIndex, state.index))

                if (trimTrailingSpace) state.skipWhitespace()
            }
        }
    }

    @Throws(ArgumentParseException::class)
    private fun TokenState.skipWhitespace() {
        while (this.hasMore && Character.isWhitespace(this.peek())) {
            this.next()
        }
    }

    @Throws(ArgumentParseException::class)
    private fun TokenState.nextArg(): String {
        val result = StringBuilder()

        if (this.hasMore) {
            when (val peek: Int = this.peek()) {
                DOUBLE_QUOTE, SINGLE_QUOTE -> this.parseQuoted(peek, result)
                else -> this.parseUnquoted(result)
            }
        }

        return result.toString()
    }

    @Throws(ArgumentParseException::class)
    private fun TokenState.parseQuoted(startQuote: Int, result: StringBuilder) {
        val current = this.next()
        if (startQuote != current) throw this.newException("Expected quote character '$startQuote' but was '$current'")

        while (true) {
            if (!this.hasMore) {
                if (this.lenient || forceLenient) {
                    return
                }
                throw this.newException("Unterminated quoted string found")
            }

            when (this.peek()) {
                startQuote -> {
                    this.next()
                    return
                }
                BACKSLASH -> this.parseEscape(result)
                else -> result.appendCodePoint(this.next())
            }
        }
    }

    @Throws(ArgumentParseException::class)
    private fun TokenState.parseUnquoted(result: StringBuilder) {
        while (this.hasMore) {
            val next = this.peek()

            when {
                Character.isWhitespace(next) -> return
                next == BACKSLASH -> this.parseEscape(result)
                else -> result.appendCodePoint(this.next())
            }
        }
    }

    @Throws(ArgumentParseException::class)
    private fun TokenState.parseEscape(result: StringBuilder) {
        this.next()
        result.appendCodePoint(this.next())
    }

    data class TokenState(val buffer: String, val lenient: Boolean) {

        var index: Int = -1
            private set

        val hasMore: Boolean get() = this.index + 1 < this.buffer.length

        @Throws(ArgumentParseException::class)
        fun peek(): Int {
            if (!this.hasMore) throw newException("Buffer overrun while parsing arguments")
            return this.buffer.codePointAt(this.index + 1)
        }

        @Throws(ArgumentParseException::class)
        fun next(): Int {
            if (!this.hasMore) throw newException("Buffer overrun while parsing arguments")
            return this.buffer.codePointAt(++this.index)
        }

        fun newException(message: String): ArgumentParseException =
            ArgumentParseException(message, this.buffer, this.index)
    }
}