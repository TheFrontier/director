package pw.dotdash.director.core.exception

open class CommandException @JvmOverloads constructor(val dynMessage: Any, val showUsage: Boolean = false) : Exception(dynMessage.toString()) {
    override val message: String = this.dynMessage.toString()
}