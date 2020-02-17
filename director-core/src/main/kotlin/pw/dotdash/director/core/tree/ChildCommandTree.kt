package pw.dotdash.director.core.tree

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.Parameter
import pw.dotdash.director.core.tree.simple.SimpleChildCommandTree

interface ChildCommandTree<in S, in P : HList<@UnsafeVariance P>, out R> : CommandTree<S, P, R> {

    val parent: CommandTree<S, P, R>

    val aliases: List<String>

    interface Builder<S, P : HList<P>, R> : CommandTree.Builder<S, P, R> {

        fun aliases(aliases: Iterable<String>): Builder<S, P, R>

        fun aliases(vararg aliases: String): Builder<S, P, R>

        override fun addChild(aliases: List<String>, init: Builder<S, P, R>.() -> Unit): Builder<S, P, R>

        override fun addChild(vararg aliases: String, init: Builder<S, P, R>.() -> Unit): Builder<S, P, R>

        override fun <NV> argument(parameter: Parameter<S, P, NV>, init: ArgumentCommandTree.Builder<S, P, NV, R>.() -> Unit): Builder<S, P, R>

        override fun executor(executor: (S, P) -> R): Builder<S, P, R>

        override fun accessibility(test: (S, P) -> Boolean): Builder<S, P, R>

        override fun description(description: Any): Builder<S, P, R>

        override fun extendedDescription(extendedDescription: Any): Builder<S, P, R>
    }
}