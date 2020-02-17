package pw.dotdash.director.core.tree

import pw.dotdash.director.core.HCons
import pw.dotdash.director.core.HList
import pw.dotdash.director.core.Parameter
import pw.dotdash.director.core.tree.simple.SimpleArgumentCommandTree

interface ArgumentCommandTree<in S, in P : HList<@UnsafeVariance P>, V, out R> : CommandTree<S, HCons<V, P>, R> {

    val parent: CommandTree<S, P, R>

    val parameter: Parameter<S, P, V>

    interface Builder<S, P : HList<P>, V, R> : CommandTree.Builder<S, HCons<V, P>, R> {

        fun setParameter(parameter: Parameter<S, P, V>): Builder<S, P, V, R>

        override fun addChild(aliases: List<String>, init: ChildCommandTree.Builder<S, HCons<V, P>, R>.() -> Unit): Builder<S, P, V, R>

        override fun addChild(vararg aliases: String, init: ChildCommandTree.Builder<S, HCons<V, P>, R>.() -> Unit): Builder<S, P, V, R>

        override fun <NV> argument(parameter: Parameter<S, HCons<V, P>, NV>, init: Builder<S, HCons<V, P>, NV, R>.() -> Unit): Builder<S, P, V, R>

        override fun executor(executor: (S, HCons<V, P>) -> R): Builder<S, P, V, R>

        override fun accessibility(test: (S, HCons<V, P>) -> Boolean): Builder<S, P, V, R>
    }
}