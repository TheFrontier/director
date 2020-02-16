package pw.dotdash.director.core.exception

import pw.dotdash.director.core.tree.CommandTree

class TreeCommandException(
    override val cause: CommandException,
    val tree: CommandTree<*, *, *>,
    val usageParts: List<String>,
    val subCommands: List<String>
) : Exception()