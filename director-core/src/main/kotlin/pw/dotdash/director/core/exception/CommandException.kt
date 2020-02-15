package pw.dotdash.director.core.exception

open class CommandException(override val message: String, val showUsage: Boolean) : Exception(message)