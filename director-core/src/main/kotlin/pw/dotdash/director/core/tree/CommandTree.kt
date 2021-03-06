package pw.dotdash.director.core.tree

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.Parameter

interface CommandTree<in S, in V : HList<@UnsafeVariance V>, out R> : TreeExecutor<S, V, R> {

    val children: Map<String, ChildCommandTree<S, V, R>>

    val primaryChildren: List<String>

    val argument: ArgumentCommandTree<S, V, *, R>?

    val executor: ((S, V) -> R)?

    val description: Any?

    val extendedDescription: Any?

    fun getUsage(source: S): String

    fun canAccess(source: S, previous: V): Boolean

    interface Builder<S, V : HList<V>, R> {

        fun addChild(aliases: List<String>, init: ChildCommandTree.Builder<S, V, R>.() -> Unit): Builder<S, V, R>

        fun addChild(vararg aliases: String, init: ChildCommandTree.Builder<S, V, R>.() -> Unit): Builder<S, V, R>

        fun <NV> argument(parameter: Parameter<S, V, NV>, init: ArgumentCommandTree.Builder<S, V, NV, R>.() -> Unit): Builder<S, V, R>

        fun executor(executor: (S, V) -> R): Builder<S, V, R>

        fun accessibility(test: (S, V) -> Boolean): Builder<S, V, R>

        fun description(description: Any): Builder<S, V, R>

        fun extendedDescription(extendedDescription: Any): Builder<S, V, R>
    }
}