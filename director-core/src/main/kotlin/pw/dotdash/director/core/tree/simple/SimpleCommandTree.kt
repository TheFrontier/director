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
import pw.dotdash.director.core.value.TransparentParameter

internal sealed class SimpleCommandTree<S, V : HList<V>, R>(
    final override val children: Map<String, SimpleChildCommandTree<S, V, R>>,
    final override val argument: SimpleArgumentCommandTree<S, V, Any?, R>?,
    final override val executor: ((S, V) -> R)?,
    private val accessibility: ((S, V) -> Boolean)?,
    final override val description: Any?,
    final override val extendedDescription: Any?
) : CommandTree<S, V, R> {

    init {
        // Setup child-parent relationships for error handling.
        for (child in this.children.values) {
            child.parent = this
        }
        this.argument?.parent = this
    }

    override val primaryChildren: List<String> = this.children.values.map { it.aliases.first() }.distinct()

    override fun canAccess(source: S, previous: V): Boolean =
        this.accessibility == null || this.accessibility.invoke(source, previous)

    @Throws(TreeCommandException::class)
    override fun execute(source: S, tokens: CommandTokens, previous: V): R {
        val snapshot: CommandTokens.Snapshot = tokens.snapshot

        if (!tokens.hasNext()) {
            // Still attempt to parse the argument, for optionals and transparents.
            if (this.argument != null) {
                try {
                    val parsed: Any? = this.argument.parameter.value.parse(source, tokens, previous)
                    val next: HCons<Any?, V> = HCons(parsed, previous)

                    if (this.argument.canAccess(source, next)) {
                        return this.argument.execute(source, tokens, next)
                    }

                    // Otherwise ignore access errors here.
                } catch (ignored: ArgumentParseException) {
                    // Ignore parsing errors since we don't have any tokens left anyways.
                    tokens.snapshot = snapshot
                } catch (e: CommandException) {
                    throw e.wrap()
                }
            }

            // Otherwise, try to execute the command.
            if (this.executor != null) {
                try {
                    return this.executor.invoke(source, previous)
                } catch (e: CommandException) {
                    throw e.wrap()
                }
            } else if (this.argument != null || this.children.isNotEmpty()) {
                throw tokens.createError("Not enough arguments!").wrap()
            } else {
                throw tokens.createError("This command has no executor.").wrap()
            }
        }

        val alias: String = tokens.next()
        val child: SimpleChildCommandTree<S, V, R>? = this.children[alias]

        if (child != null) {
            if (!child.canAccess(source, previous)) {
                throw tokens.createError("You do not have access to this subcommand.").wrap()
            }

            return child.execute(source, tokens, previous)
        }

        tokens.snapshot = snapshot

        if (this.argument != null) {
            try {
                val parsed: Any? = this.argument.parameter.value.parse(source, tokens, previous)
                val next: HCons<Any?, V> = HCons(parsed, previous)

                if (!this.argument.canAccess(source, next)) {
                    throw tokens.createError("You do not have access to this argument.").wrap()
                }

                return this.argument.execute(source, tokens, next)
            } catch (e: CommandException) {
                throw e.wrap()
            }
        }

        throw tokens.createError("Too many arguments!").wrap()
    }

    @Throws(TreeCommandException::class)
    override fun complete(source: S, tokens: CommandTokens, previous: V): List<String> {
        val snapshot: CommandTokens.Snapshot = tokens.snapshot

        if (!tokens.hasNext()) {
            if (this.argument != null) {
                try {
                    val parsed: Any? = this.argument.parameter.value.parse(source, tokens, previous)
                    val next: HCons<Any?, V> = HCons(parsed, previous)

                    // Check for access by the source first.
                    if (this.argument.canAccess(source, next)) {
                        val completions: List<String> = this.argument.complete(source, tokens, next)

                        if (this.argument.parameter.value is TransparentParameter) {
                            // Also return the current tree's completions since it's transparent.
                            return completions + this.subCompletions(source, tokens, previous)
                        }

                        return completions
                    }

                    // Otherwise ignore access errors here.
                } catch (ignored: ArgumentParseException) {
                    // Ignore parsing errors since we don't have any tokens left anyways.
                    tokens.snapshot = snapshot
                } catch (e: CommandException) {
                    throw e.wrap()
                }
            }

            return this.subCompletions(source, tokens, previous)
        }

        val alias: String = tokens.next()
        val child: SimpleChildCommandTree<S, V, R>? = this.children[alias]

        if (tokens.hasNext()) {
            // Check for access by the source first.
            if (child != null && child.canAccess(source, previous)) {
                // Child found; complete its subtree.
                return child.complete(source, tokens, previous)
            }

            // Otherwise ignore access errors since we're completing.
        } else {
            // Extra filtering for nicer tab completion.
            return this.subCompletions(source, tokens, previous).filter(StartsWithPredicate(alias))
        }

        tokens.snapshot = snapshot

        // Try the child argument.
        if (this.argument != null) {
            try {
                val parsed: Any? = this.argument.parameter.value.parse(source, tokens, previous)
                val next: HCons<Any?, V> = HCons(parsed, previous)

                // Check for access by the source first.
                if (this.argument.canAccess(source, next)) {
                    // Argument successfully parsed; complete its subtree.
                    return this.argument.complete(source, tokens, next)
                }

                // Otherwise ignore access errors since we're completing.
            } catch (e: CommandException) {
                // Failed to parse argument; rollback.
                tokens.snapshot = snapshot
            }
        }

        return this.subCompletions(source, tokens, previous)
    }

    private fun subCompletions(source: S, tokens: CommandTokens, previous: V): List<String> {
        val result = HashSet<String>()

        for (child: SimpleChildCommandTree<S, V, R> in this.children.values) {
            // Add the child's main alias, if the source has access.
            if (child.canAccess(source, previous)) {
                result += child.aliases.first()
            }
        }

        if (this.argument != null) {
            try {
                result += this.argument.parameter.value.complete(source, tokens, previous)
            } catch (e: CommandException) {
                throw e.wrap()
            }
        }

        return result.toList()
    }

    private fun CommandException.wrap(): TreeCommandException =
        TreeCommandException(cause = this, tree = this@SimpleCommandTree)

    override fun getUsage(source: S): String {
        val builder = StringBuilder()
        // Go back up the tree, appending the usage from beforehand.
        appendUsageFromRoot(builder, source, this)

        // Append usage of any successive arguments.
        var argument: ArgumentCommandTree<S, *, *, *>? = this.argument
        while (argument != null) {
            builder.append(' ').append(argument.parameter.getUsage(source))
            argument = argument.argument
        }

        return builder.toString()
    }

    private fun appendUsageFromRoot(builder: StringBuilder, source: S, tree: CommandTree<S, *, *>) {
        when (tree) {
            is RootCommandTree<S, *, *> -> {
                builder.append(tree.aliases.first())
            }
            is ChildCommandTree<S, *, *> -> {
                appendUsageFromRoot(builder, source, tree.parent)
                builder.append(' ').append(tree.aliases.first())
            }
            is ArgumentCommandTree<S, *, *, *> -> {
                appendUsageFromRoot(builder, source, tree.parent)
                builder.append(' ').append(tree.parameter.getUsage(source))
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    abstract class Builder<B : Builder<B, S, V, R>, S, V : HList<V>, R> : CommandTree.Builder<S, V, R> {

        protected val children = HashMap<String, SimpleChildCommandTree<S, V, R>>()
        protected var argument: SimpleArgumentCommandTree<S, V, in Any?, R>? = null
        protected var executor: ((S, V) -> R)? = null
        protected var accessibility: ((S, V) -> Boolean)? = null
        protected var description: Any? = null
        protected var extendedDescription: Any? = null

        override fun addChild(aliases: List<String>, init: ChildCommandTree.Builder<S, V, R>.() -> Unit): B {
            val child: SimpleChildCommandTree<S, V, R> = SimpleChildCommandTree.Builder<S, V, R>().aliases(aliases).apply(init).build()
            for (alias: String in child.aliases) {
                require(alias !in this.children) { "Child alias $alias is already registered." }
                this.children[alias] = child
            }
            return this as B
        }

        override fun addChild(vararg aliases: String, init: ChildCommandTree.Builder<S, V, R>.() -> Unit): B =
            this.addChild(aliases.toList(), init)

        override fun <NV> argument(parameter: Parameter<S, V, NV>, init: ArgumentCommandTree.Builder<S, V, NV, R>.() -> Unit): B {
            this.argument = SimpleArgumentCommandTree.Builder<S, V, NV, R>().parameter(parameter).apply(init).build()
                    as SimpleArgumentCommandTree<S, V, in Any?, R>
            return this as B
        }

        override fun executor(executor: (S, V) -> R): B {
            this.executor = executor
            return this as B
        }

        override fun accessibility(test: (S, V) -> Boolean): B {
            this.accessibility = test
            return this as B
        }

        override fun description(description: Any): B {
            this.description = description
            return this as B
        }

        override fun extendedDescription(extendedDescription: Any): B {
            this.extendedDescription = extendedDescription
            return this as B
        }
    }
}

internal class SimpleRootCommandTree<S, V : HList<V>, R>(
    override val aliases: List<String>,
    override val initial: V,
    override val tokenizer: InputTokenizer,
    children: Map<String, SimpleChildCommandTree<S, V, R>>,
    argument: SimpleArgumentCommandTree<S, V, in Any?, R>?,
    executor: ((S, V) -> R)?,
    accessibility: ((S, V) -> Boolean)?,
    description: Any?,
    extendedDescription: Any?
) : SimpleCommandTree<S, V, R>(children, argument, executor, accessibility, description, extendedDescription),
    RootCommandTree<S, V, R> {

    class Builder<S, V : HList<V>, R> :
        SimpleCommandTree.Builder<Builder<S, V, R>, S, V, R>(),
        RootCommandTree.Builder<S, V, R> {

        private var aliases: List<String>? = null
        private var initial: V? = null
        private var tokenizer: InputTokenizer = QuotedInputTokenizer.DEFAULT

        override fun aliases(aliases: Iterable<String>): Builder<S, V, R> {
            this.aliases = aliases.toList()
            return this
        }

        override fun aliases(vararg aliases: String): Builder<S, V, R> {
            this.aliases = aliases.toList()
            return this
        }

        override fun initial(initial: V): Builder<S, V, R> {
            this.initial = initial
            return this
        }

        override fun tokenizer(tokenizer: InputTokenizer): Builder<S, V, R> {
            this.tokenizer = tokenizer
            return this
        }

        override fun build(): SimpleRootCommandTree<S, V, R> = SimpleRootCommandTree(
            aliases = checkNotNull(this.aliases),
            initial = checkNotNull(this.initial),
            tokenizer = checkNotNull(this.tokenizer),
            children = this.children,
            argument = this.argument,
            executor = this.executor,
            accessibility = this.accessibility,
            description = this.description,
            extendedDescription = this.extendedDescription
        )
    }
}

internal class SimpleChildCommandTree<S, P : HList<P>, R>(
    override val aliases: List<String>,
    children: Map<String, SimpleChildCommandTree<S, P, R>>,
    argument: SimpleArgumentCommandTree<S, P, in Any?, R>?,
    executor: ((S, P) -> R)?,
    accessibility: ((S, P) -> Boolean)?,
    description: Any?,
    extendedDescription: Any?
) : SimpleCommandTree<S, P, R>(children, argument, executor, accessibility, description, extendedDescription),
    ChildCommandTree<S, P, R> {

    override lateinit var parent: CommandTree<S, P, R>

    class Builder<S, P : HList<P>, R> :
        SimpleCommandTree.Builder<Builder<S, P, R>, S, P, R>(),
        ChildCommandTree.Builder<S, P, R> {

        private var aliases: List<String>? = null

        override fun aliases(aliases: Iterable<String>): Builder<S, P, R> {
            this.aliases = aliases.toList()
            return this
        }

        override fun aliases(vararg aliases: String): Builder<S, P, R> {
            this.aliases = aliases.toList()
            return this
        }

        fun build(): SimpleChildCommandTree<S, P, R> = SimpleChildCommandTree(
            aliases = checkNotNull(this.aliases),
            children = this.children,
            argument = this.argument,
            executor = this.executor,
            accessibility = this.accessibility,
            description = this.description,
            extendedDescription = this.description
        )
    }
}

internal class SimpleArgumentCommandTree<S, P : HList<P>, V, R>(
    override val parameter: Parameter<S, P, V>,
    children: Map<String, SimpleChildCommandTree<S, HCons<V, P>, R>>,
    argument: SimpleArgumentCommandTree<S, HCons<V, P>, in Any?, R>?,
    executor: ((S, HCons<V, P>) -> R)?,
    accessibility: ((S, HCons<V, P>) -> Boolean)?,
    description: Any?,
    extendedDescription: Any?
) : SimpleCommandTree<S, HCons<V, P>, R>(children, argument, executor, accessibility, description, extendedDescription),
    ArgumentCommandTree<S, P, V, R> {

    override lateinit var parent: CommandTree<S, P, R>

    class Builder<S, P : HList<P>, V, R> :
        SimpleCommandTree.Builder<Builder<S, P, V, R>, S, HCons<V, P>, R>(),
        ArgumentCommandTree.Builder<S, P, V, R> {

        private var parameter: Parameter<S, P, V>? = null

        override fun parameter(parameter: Parameter<S, P, V>): Builder<S, P, V, R> {
            this.parameter = parameter
            return this
        }

        fun build(): SimpleArgumentCommandTree<S, P, V, R> = SimpleArgumentCommandTree(
            parameter = checkNotNull(this.parameter),
            children = this.children,
            argument = this.argument,
            executor = this.executor,
            accessibility = this.accessibility,
            description = this.description,
            extendedDescription = this.extendedDescription
        )
    }
}