package pw.dotdash.director.core.lexer

import pw.dotdash.director.core.exception.ArgumentParseException
import java.util.*

interface CommandTokens {

    val index: Int

    operator fun hasNext(): Boolean

    @Throws(ArgumentParseException::class)
    fun peek(): String

    fun peekIfPresent(): String?

    @Throws(ArgumentParseException::class)
    operator fun next(): String

    fun nextIfPresent(): String?

    fun createError(message: String): ArgumentParseException

    val raw: String

    val tokens: List<Token>

    fun insertToken(value: String)

    fun removeTokens(from: Snapshot, to: Snapshot)

    fun previous()

    val rawPosition: Int

    var snapshot: Snapshot

    fun goto(snapshot: Snapshot)

    interface Snapshot
}