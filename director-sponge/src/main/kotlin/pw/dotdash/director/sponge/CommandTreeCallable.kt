package pw.dotdash.director.sponge

import org.spongepowered.api.command.CommandCallable
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.Text.NEW_LINE
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
import pw.dotdash.director.core.tree.RootCommandTree
import java.util.*


class CommandTreeCallable<T : HList<T>> @JvmOverloads constructor(
    private val root: RootCommandTree<CommandSource, T, CommandResult>,
    private val initial: T,
    private val permission: String? = null
) : CommandCallable {

    companion object {
        private val ERROR_FROM: Text = Text.of(ITALIC, "Error from ")
        private val COLON: Text = Text.of(":")
        private val USAGE: Text = Text.of("Usage: ")
        private val SUB_COMMANDS: Text = Text.of("Subcommands: ")
        private val COMMA_SEPARATOR: Text = Text.of(", ")

        @JvmStatic
        @JvmName("of")
        @JvmOverloads
        operator fun invoke(root: RootCommandTree<CommandSource, HNil, CommandResult>, permission: String? = null): CommandTreeCallable<HNil> =
            CommandTreeCallable(root, HNil, permission)
    }

    private val rootAlias: Text = Text.of(YELLOW, this.root.aliases.first())

    override fun process(source: CommandSource, arguments: String): CommandResult {
        try {
            val tokens = SimpleCommandTokens(arguments, this.root.tokenizer.tokenize(arguments, false).toMutableList())
            return this.root.execute(source, tokens, this.initial)
        } catch (e: TreeCommandException) {
            throw CommandException(createErrorMessage(e))
        }
    }

    override fun getSuggestions(source: CommandSource, arguments: String, targetPosition: Location<World>?): List<String> {
        try {
            val tokens = SimpleCommandTokens(arguments, this.root.tokenizer.tokenize(arguments, true).toMutableList())
            return this.root.complete(source, tokens, this.initial)
        } catch (e: TreeCommandException) {
            throw CommandException(createErrorMessage(e))
        }
    }

    private fun createErrorMessage(e: TreeCommandException): Text {
        val cause: pw.dotdash.director.core.exception.CommandException = e.cause

        val builder: Text.Builder = Text.builder().color(RED)
            .append(ERROR_FROM, this.rootAlias, COLON, NEW_LINE, Text.of(cause.message))

        if (cause is ArgumentParseException) {
            builder.append(NEW_LINE, Text.of(cause.annotatedPosition))
        }

        if (e.cause.showUsage) {
            builder.append(NEW_LINE, NEW_LINE, USAGE, this.rootAlias, Text.of(YELLOW, " ", e.usageParts.joinToString(separator = " ")))

            if (e.subCommands.isNotEmpty()) {
                builder.append(SUB_COMMANDS, Text.joinWith(COMMA_SEPARATOR, e.subCommands.map { Text.of(YELLOW, it) }))
            }
        }

        return builder.build()
    }

    override fun testPermission(source: CommandSource): Boolean =
        this.permission == null || source.hasPermission(this.permission)

    override fun getShortDescription(source: CommandSource): Optional<Text> =
        Optional.ofNullable(this.root.description?.let(Text::of))

    override fun getHelp(source: CommandSource): Optional<Text> {
        val builder: Text.Builder = Text.builder()
        this.root.description?.let { builder.append(Text.of(it), NEW_LINE) }
        builder.append(getUsage(source))
        this.root.extendedDescription?.let { builder.append(NEW_LINE, Text.of(it)) }
        return Optional.of(builder.build())
    }

    override fun getUsage(source: CommandSource): Text = Text.of(this.root.getUsage(source))
}