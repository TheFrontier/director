package pw.dotdash.director.core.tree

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.Parameter

interface CommandTree<in S, in V : HList<@UnsafeVariance V>, out R> : TreeExecutor<S, V, R> {

    val children: Map<String, ChildCommandTree<S, V, R>>

    val argument: ArgumentCommandTree<S, V, *, R>?

    val executor: ((S, V) -> R)?

    fun getUsage(source: S): String

    fun canAccess(source: S, previous: V): Boolean

    interface Builder<S, V : HList<V>, R> {

        fun addChild(child: ChildCommandTree<S, V, R>): Builder<S, V, R>

        fun addChild(aliases: List<String>, init: ChildCommandTree.Builder<S, V, R>.() -> Unit): Builder<S, V, R>

        fun addChild(vararg aliases: String, init: ChildCommandTree.Builder<S, V, R>.() -> Unit): Builder<S, V, R>

        fun setArgument(argument: ArgumentCommandTree<S, V, *, R>): Builder<S, V, R>

        fun <NV> setArgument(parameter: Parameter<S, V, NV>, init: ArgumentCommandTree.Builder<S, V, NV, R>.() -> Unit): Builder<S, V, R>

        fun setExecutor(executor: (S, V) -> R): Builder<S, V, R>

        fun setAccessibility(test: (S, V) -> Boolean): Builder<S, V, R>
    }
}