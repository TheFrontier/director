package pw.dotdash.director.core.exception

class ArgumentParseException(override val message: String, val source: String, val position: Int) : CommandException(message, true)