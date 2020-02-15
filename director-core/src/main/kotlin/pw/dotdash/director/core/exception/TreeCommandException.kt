package pw.dotdash.director.core.exception

class TreeCommandException(
    override val cause: CommandException,
    val usageParts: List<String>,
    val subCommands: List<String>
) : Exception()