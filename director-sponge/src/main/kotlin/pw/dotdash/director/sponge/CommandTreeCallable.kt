package pw.dotdash.director.sponge

import org.spongepowered.api.command.*
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.Text.NEW_LINE
import org.spongepowered.api.text.TextRepresentable
import org.spongepowered.api.text.format.TextColors.RED
import org.spongepowered.api.text.format.TextColors.YELLOW
import org.spongepowered.api.text.format.TextStyles.ITALIC
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import pw.dotdash.director.core.HList
import pw.dotdash.director.core.HNil
import pw.dotdash.director.core.exception.ArgumentParseException
import pw.dotdash.director.core.exception.TreeCommandException
import pw.dotdash.director.core.lexer.SimpleCommandTokens
import pw.dotdash.director.core.tree.CommandTree
import pw.dotdash.director.core.tree.RootCommandTree
import java.util.*


class CommandTreeCallable<V : HList<V>>(
    private val root: RootCommandTree<CommandSource, V, CommandResult>,
    private val initial: V
) : CommandCallable {

    companion object {
        private val ERROR_FROM: Text = Text.of(ITALIC, "Error from ")
        private val COLON: Text = Text.of(":")
        private val USAGE: Text = Text.of("Usage: ")
        private val SUB_COMMANDS: Text = Text.of("Subcommands: ")
        private val COMMA_SEPARATOR: Text = Text.of(", ")

        @JvmStatic
        @JvmName("of")
        operator fun invoke(root: RootCommandTree<CommandSource, HNil, CommandResult>): CommandTreeCallable<HNil> =
            CommandTreeCallable(root, HNil)
    }

    private val rootAlias: Text = Text.of("/", YELLOW, this.root.aliases.first())

    override fun process(source: CommandSource, arguments: String): CommandResult {
        if (!this.testPermission(source)) {
            throw CommandPermissionException()
        }

        try {
            val tokens = SimpleCommandTokens(arguments, this.root.tokenizer.tokenize(arguments, false).toMutableList())
            return this.root.execute(source, tokens, this.initial)
        } catch (e: TreeCommandException) {
            throw CommandException(createErrorMessage(source, e))
        }
    }

    override fun getSuggestions(source: CommandSource, arguments: String, targetPosition: Location<World>?): List<String> {
        if (!this.testPermission(source)) {
            return emptyList()
        }

        try {
            val tokens = SimpleCommandTokens(arguments, this.root.tokenizer.tokenize(arguments, true).toMutableList())
            return this.root.complete(source, tokens, this.initial)
        } catch (e: TreeCommandException) {
            throw CommandException(createErrorMessage(source, e))
        }
    }

    private fun createErrorMessage(source: CommandSource, e: TreeCommandException): Text {
        val cause: pw.dotdash.director.core.exception.CommandException = e.cause
        @Suppress("UNCHECKED_CAST")
        val tree: CommandTree<CommandSource, *, *> = e.tree as CommandTree<CommandSource, *, *>

        val errorMessage: Text = cause.dynMessage.let { (it as? TextRepresentable)?.toText() } ?: Text.of(cause.message)

        val builder: Text.Builder = Text.builder().color(RED)
            .append(ERROR_FROM, this.rootAlias, COLON, NEW_LINE, errorMessage)

        if (cause is ArgumentParseException) {
            val annotated: String = cause.annotatedPosition
            if (annotated.isNotBlank()) {
                builder.append(NEW_LINE, Text.of(annotated))
            }
        }

        if (e.cause.showUsage) {
            builder.append(NEW_LINE, NEW_LINE, USAGE, Text.of(YELLOW, tree.getUsage(source)))

            if (tree.primaryChildren.isNotEmpty()) {
                builder.append(NEW_LINE, SUB_COMMANDS, Text.joinWith(COMMA_SEPARATOR, tree.primaryChildren.map { Text.of(YELLOW, it) }))
            }
        }

        return builder.build()
    }

    private fun Any.toText(): Text = (this as? TextRepresentable)?.toText() ?: Text.of(this)

    override fun testPermission(source: CommandSource): Boolean =
        this.root.canAccess(source, this.initial)

    override fun getShortDescription(source: CommandSource): Optional<Text> =
        Optional.ofNullable(this.root.description?.toText())

    override fun getHelp(source: CommandSource): Optional<Text> {
        val builder: Text.Builder = Text.builder()
        this.root.description?.toText()?.let { builder.append(it, NEW_LINE) }
        builder.append(getUsage(source))
        this.root.extendedDescription?.toText()?.let { builder.append(NEW_LINE, it) }
        return Optional.of(builder.build())
    }

    override fun getUsage(source: CommandSource): Text = Text.of(this.root.getUsage(source))
}