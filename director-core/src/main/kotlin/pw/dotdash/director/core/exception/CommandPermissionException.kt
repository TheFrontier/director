package pw.dotdash.director.core.exception

class CommandPermissionException(message: String = "You do not have permission to use this command.") : CommandException(message, true)