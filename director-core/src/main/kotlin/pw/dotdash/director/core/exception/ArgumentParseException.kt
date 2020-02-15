package pw.dotdash.director.core.exception

import kotlin.math.min

class ArgumentParseException(override val message: String, val source: String, val position: Int) : CommandException(message, true) {

    val annotatedPosition: String get() {
        if (source.isBlank()) return ""

        var source: String = this.source
        var position: Int = this.position

        if (source.length > 80) {
            if (position >= 37) {
                val start = position - 37
                val end = min(source.length, position + 37)

                source = if (end < source.length) {
                    "..." + source.substring(start, end) + "..."
                } else {
                    "..." + source.substring(start, end)
                }
                position -= 40
            } else {
                source = source.substring(0, 77) + "..."
            }
        }

        return source + "\n" + " ".repeat(position) + "^"
    }
}