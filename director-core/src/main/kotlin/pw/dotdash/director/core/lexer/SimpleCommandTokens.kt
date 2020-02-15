package pw.dotdash.director.core.lexer

import pw.dotdash.director.core.exception.ArgumentParseException

data class SimpleCommandTokens(override val raw: String, override val tokens: MutableList<Token>) : CommandTokens {

    override var index: Int = -1
        private set

    override fun hasNext(): Boolean = this.index + 1 < this.tokens.size

    override fun peek(): String {
        if (!this.hasNext()) throw this.createError("Not enough arguments!")
        return this.tokens[this.index + 1].value
    }

    override fun peekIfPresent(): String? =
        this.tokens.getOrNull(this.index + 1)?.value

    override fun next(): String {
        if (!this.hasNext()) throw this.createError("Not enough arguments!")
        return this.tokens[++this.index].value
    }

    override fun nextIfPresent(): String? =
        this.tokens.getOrNull(++this.index)?.value

    override fun createError(message: String): ArgumentParseException =
        ArgumentParseException(message, this.raw, this.rawPosition)

    override fun insertToken(value: String) {
        val index: Int = if (this.index < 0) 0 else this.tokens[this.index].endIndex
        this.tokens.add(this.index + 1, Token(value, index, index))
    }

    override fun removeTokens(from: CommandTokens.Snapshot, to: CommandTokens.Snapshot) {
        require(from is Snapshot && to is Snapshot) { "Invalid token snapshot format." }

        if (this.index >= from.index) {
            if (this.index < to.index) {
                this.index = from.index - 1
            } else {
                this.index -= (to.index - from.index) + 1
            }
        }

        for (i: Int in from.index..to.index) {
            this.tokens.removeAt(from.index)
        }
    }

    override fun previous() {
        if (this.index > -1) {
            --this.index
        }
    }

    override val rawPosition: Int
        get() = this.tokens.getOrNull(this.index)?.startIndex ?: 0

    override var snapshot: CommandTokens.Snapshot
        get() = Snapshot(this.index, this.tokens.toList())
        set(value) {
            require(value is Snapshot) { "Invalid token snapshot format." }
            this.index = value.index
            this.tokens.clear()
            this.tokens += value.tokens
        }

    override fun goto(snapshot: CommandTokens.Snapshot) {
        require (snapshot is Snapshot) { "Invalid token snapshot format." }
        this.index = snapshot.index
    }

    private data class Snapshot(val index: Int, val tokens: List<Token>) : CommandTokens.Snapshot
}