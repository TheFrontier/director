package pw.dotdash.director.core.tree.simple

import pw.dotdash.director.core.HCons
import pw.dotdash.director.core.HList
import pw.dotdash.director.core.Parameter
import pw.dotdash.director.core.exception.ArgumentParseException
import pw.dotdash.director.core.exception.CommandException
import pw.dotdash.director.core.exception.TreeCommandException
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.lexer.InputTokenizer
import pw.dotdash.director.core.lexer.QuotedInputTokenizer
import pw.dotdash.director.core.tree.*
import pw.dotdash.director.core.util.StartsWithPredicate
import java.util.function.Consumer

internal sealed class SimpleCommandTree<S, V : HList<V>, R>(
    final override val children: Map<String, SimpleChildCommandTree<S, V, R>>,
    final override val argument: SimpleArgumentCommandTree<S, V, Any?, R>?,
    final override val executor: ((S, V) -> R)?
) : CommandTree<S, V, R> {

    override fun execute(source: S, tokens: CommandTokens, previous: V): R {
        return execute(source, tokens, previous, ArrayList())
    }

    override fun complete(source: S, tokens: CommandTokens, previous: V): List<String> {
        return complete(source, tokens, previous, ArrayList())
    }

    @Throws(TreeCommandException::class)
    protected fun execute(source: S, tokens: CommandTokens, previous: V, usageParts: MutableList<String>): R {
        val snapshot: CommandTokens.Snapshot = tokens.snapshot

        if (!tokens.hasNext()) {
            // Still attempt to parse the argument.
            if (this.argument != null) {
                try {
                    val parsed: Any? = this.argument.parameter.value.parse(source, tokens, previous)
                    usageParts += this.argument.parameter.getUsage(source)

                    return this.argument.execute(source, tokens, HCons(parsed, previous), usageParts)
                } catch (ignored: ArgumentParseException) {
                    // Ignore parsing errors since we don't have any tokens left.
                    tokens.snapshot = snapshot
                } catch (e: CommandException) {
                    throw e.wrap(usageParts)
                }
            }

            // Otherwise, try to execute the command.
            if (this.executor != null) {
                try {
                    return this.executor.invoke(source, previous)
                } catch (e: CommandException) {
                    throw e.wrap(usageParts)
                }
            } else if (this.argument != null || this.children.isNotEmpty()) {
                tokens.nextIfPresent()
                throw tokens.createError("Not enough arguments!").wrap(usageParts)
            } else {
                throw tokens.createError("This command has no executor.").wrap(usageParts)
            }
        }

        val alias: String = tokens.next()
        val child: SimpleChildCommandTree<S, V, R>? = this.children[alias]

        if (child != null) {
            usageParts += alias

            return child.execute(source, tokens, previous, usageParts)
        }

        tokens.snapshot = snapshot

        if (this.argument != null) {
            try {
                val parsed: Any? = this.argument.parameter.value.parse(source, tokens, previous)
                usageParts += this.argument.parameter.getUsage(source)

                return this.argument.execute(source, tokens, HCons(parsed, previous), usageParts)
            } catch (e: CommandException) {
                throw e.wrap(usageParts)
            }
        }

        throw tokens.createError("Invalid subcommand.").wrap(usageParts)
    }

    protected fun complete(source: S, tokens: CommandTokens, previous: V, usageParts: MutableList<String>): List<String> {
        val snapshot: CommandTokens.Snapshot = tokens.snapshot

        if (!tokens.hasNext()) {
            if (this.argument != null) {
                try {
                    val parsed: Any? = this.argument.parameter.value.parse(source, tokens, previous)
                    usageParts += this.argument.parameter.getUsage(source)

                    return this.argument.complete(source, tokens, HCons(parsed, previous), usageParts)
                } catch (ignored: ArgumentParseException) {
                    // Ignore parsing errors since we don't have any tokens left.
                    tokens.snapshot = snapshot
                } catch (e: CommandException) {
                    throw e.wrap(usageParts)
                }
            }

            return this.subCompletions(source, tokens, previous, usageParts)
        }

        val alias: String = tokens.next()
        val child: SimpleChildCommandTree<S, V, R>? = this.children[alias]

        if (tokens.hasNext()) {
            if (child != null) {
                usageParts += alias

                return child.complete(source, tokens, previous, usageParts)
            }
        } else {
            return this.subCompletions(source, tokens, previous, usageParts).filter(StartsWithPredicate(alias))
        }

        tokens.snapshot = snapshot

        // Try the child argument.
        if (this.argument != null) {
            try {
                val parsed: Any? = this.argument.parameter.value.parse(source, tokens, previous)
                usageParts += this.argument.parameter.getUsage(source)

                // Argument successfully parsed; complete its subtree.
                return this.argument.complete(source, tokens, HCons(parsed, previous), usageParts)
            } catch (e: CommandException) {
                // Failed to parse argument; rollback.
                tokens.snapshot = snapshot
            }
        }

        return this.subCompletions(source, tokens, previous, usageParts)
    }

    private fun subCompletions(source: S, tokens: CommandTokens, previous: V, usageParts: MutableList<String>): List<String> {
        val result = ArrayList<String>()

        result += this.children.keys

        if (this.argument != null) {
            try {
                result += this.argument.parameter.value.complete(source, tokens, previous)
            } catch (e: CommandException) {
                throw e.wrap(usageParts)
            }
        }

        return result
    }

    private fun CommandException.wrap(usageParts: List<String>): TreeCommandException =
        TreeCommandException(this, this@SimpleCommandTree, usageParts, this@SimpleCommandTree.children.keys.toList())

    private val argSequence: Sequence<SimpleArgumentCommandTree<S, *, *, R>> =
        generateSequence<SimpleArgumentCommandTree<S, *, *, R>>(this.argument) {
            it.argument
        }

    override fun getUsage(source: S): String {
        val builder = StringBuilder()

        if (this.children.isNotEmpty()) {
            this.children.keys.joinTo(builder, separator = "|", postfix = "|")
        }

        this.argSequence.joinTo(builder, separator = " ") { it.parameter.getUsage(source) }

        return builder.toString()
    }

    @Suppress("UNCHECKED_CAST")
    abstract class Builder<B : Builder<B, S, V, R>, S, V : HList<V>, R> : CommandTree.Builder<S, V, R> {

        protected val children = HashMap<String, SimpleChildCommandTree<S, V, R>>()
        protected var argument: SimpleArgumentCommandTree<S, V, in Any?, R>? = null
        protected var executor: ((S, V) -> R)? = null

        override fun addChild(child: ChildCommandTree<S, V, R>): B {
            require(child is SimpleChildCommandTree) { "Child trees must be made with ChildCommandTree.builder()" }

            for (alias: String in child.aliases) {
                require(alias !in this.children) { "Child alias $alias is already registered." }
                this.children[alias] = child
            }
            return this as B
        }

        override fun addChild(aliases: List<String>, init: ChildCommandTree.Builder<S, V, R>.() -> Unit): B =
            this.addChild(ChildCommandTree.builder<S, V, R>().setAliases(aliases).apply(init).build())

        override fun addChild(vararg aliases: String, init: ChildCommandTree.Builder<S, V, R>.() -> Unit): B =
            this.addChild(ChildCommandTree.builder<S, V, R>().setAliases(*aliases).apply(init).build())

        override fun setArgument(argument: ArgumentCommandTree<S, V, *, R>): B {
            require(argument is SimpleArgumentCommandTree) { "Argument trees must be made with ArgumentCommandTree.builder()" }

            this.argument = argument as SimpleArgumentCommandTree<S, V, in Any?, R>
            return this as B
        }

        override fun <NV> setArgument(parameter: Parameter<S, V, NV>, init: ArgumentCommandTree.Builder<S, V, NV, R>.() -> Unit): B =
            this.setArgument(ArgumentCommandTree.builder<S, V, NV, R>().setParameter(parameter).apply(init).build())

        override fun setExecutor(executor: (S, V) -> R): B {
            this.executor = executor
            return this as B
        }
    }
}

internal class SimpleRootCommandTree<S, V : HList<V>, R>(
    override val aliases: List<String>,
    override val initial: V,
    override val tokenizer: InputTokenizer,
    override val description: String?,
    override val extendedDescription: String?,
    children: Map<String, SimpleChildCommandTree<S, V, R>>,
    argument: SimpleArgumentCommandTree<S, V, in Any?, R>?,
    executor: ((S, V) -> R)?
) : SimpleCommandTree<S, V, R>(children, argument, executor), RootCommandTree<S, V, R> {

    class Builder<S, V : HList<V>, R> :
        SimpleCommandTree.Builder<Builder<S, V, R>, S, V, R>(),
        RootCommandTree.Builder<S, V, R> {

        private var aliases: List<String>? = null
        private var initial: V? = null
        private var tokenizer: InputTokenizer = QuotedInputTokenizer.DEFAULT
        private var description: String? = null
        private var extendedDescription: String? = null

        override fun setAliases(aliases: Iterable<String>): Builder<S, V, R> {
            this.aliases = aliases.toList()
            return this
        }

        override fun setAliases(vararg aliases: String): Builder<S, V, R> {
            this.aliases = aliases.toList()
            return this
        }

        override fun setInitial(initial: V): Builder<S, V, R> {
            this.initial = initial
            return this
        }

        override fun setTokenizer(tokenizer: InputTokenizer): Builder<S, V, R> {
            this.tokenizer = tokenizer
            return this
        }

        override fun setDescription(description: String): RootCommandTree.Builder<S, V, R> {
            this.description = description
            return this
        }

        override fun setExtendedDescription(extendedDescription: String): RootCommandTree.Builder<S, V, R> {
            this.extendedDescription = extendedDescription
            return this
        }

        override fun build(): RootCommandTree<S, V, R> = SimpleRootCommandTree(
            aliases = checkNotNull(this.aliases),
            initial = checkNotNull(this.initial),
            tokenizer = checkNotNull(this.tokenizer),
            description = this.description,
            extendedDescription = this.extendedDescription,
            children = this.children,
            argument = this.argument,
            executor = this.executor
        )
    }
}

internal class SimpleChildCommandTree<S, P : HList<P>, R>(
    override val aliases: List<String>,
    children: Map<String, SimpleChildCommandTree<S, P, R>>,
    argument: SimpleArgumentCommandTree<S, P, in Any?, R>?,
    executor: ((S, P) -> R)?
) : SimpleCommandTree<S, P, R>(children, argument, executor), ChildCommandTree<S, P, R> {

    class Builder<S, P : HList<P>, R> :
        SimpleCommandTree.Builder<Builder<S, P, R>, S, P, R>(),
        ChildCommandTree.Builder<S, P, R> {

        private var aliases: List<String>? = null

        override fun setAliases(aliases: Iterable<String>): Builder<S, P, R> {
            this.aliases = aliases.toList()
            return this
        }

        override fun setAliases(vararg aliases: String): Builder<S, P, R> {
            this.aliases = aliases.toList()
            return this
        }

        override fun build(): ChildCommandTree<S, P, R> = SimpleChildCommandTree(
            aliases = checkNotNull(this.aliases),
            children = this.children,
            argument = this.argument,
            executor = this.executor
        )
    }
}

internal class SimpleArgumentCommandTree<S, P : HList<P>, V, R>(
    override val parameter: Parameter<S, P, V>,
    children: Map<String, SimpleChildCommandTree<S, HCons<V, P>, R>>,
    argument: SimpleArgumentCommandTree<S, HCons<V, P>, in Any?, R>?,
    executor: ((S, HCons<V, P>) -> R)?
) : SimpleCommandTree<S, HCons<V, P>, R>(children, argument, executor), ArgumentCommandTree<S, P, V, R> {

    class Builder<S, P : HList<P>, V, R> :
        SimpleCommandTree.Builder<Builder<S, P, V, R>, S, HCons<V, P>, R>(),
        ArgumentCommandTree.Builder<S, P, V, R> {

        private var parameter: Parameter<S, P, V>? = null

        override fun setParameter(parameter: Parameter<S, P, V>): ArgumentCommandTree.Builder<S, P, V, R> {
            this.parameter = parameter
            return this
        }

        override fun build(): ArgumentCommandTree<S, P, V, R> = SimpleArgumentCommandTree(
            parameter = checkNotNull(this.parameter),
            children = this.children,
            argument = this.argument,
            executor = this.executor
        )
    }
}