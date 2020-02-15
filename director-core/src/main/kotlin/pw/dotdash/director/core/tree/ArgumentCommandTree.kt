package pw.dotdash.director.core.tree

import pw.dotdash.director.core.HCons
import pw.dotdash.director.core.HList
import pw.dotdash.director.core.Parameter
import pw.dotdash.director.core.tree.simple.SimpleArgumentCommandTree

interface ArgumentCommandTree<in S, in P : HList<@UnsafeVariance P>, V, out R> : CommandTree<S, HCons<V, P>, R> {

    val parameter: Parameter<S, P, V>

    interface Builder<S, P : HList<P>, V, R> : CommandTree.Builder<S, HCons<V, P>, R> {

        fun setParameter(parameter: Parameter<S, P, V>): Builder<S, P, V, R>

        override fun addChild(child: ChildCommandTree<S, HCons<V, P>, R>): Builder<S, P, V, R>

        override fun setArgument(argument: ArgumentCommandTree<S, HCons<V, P>, *, R>): Builder<S, P, V, R>

        override fun setExecutor(executor: CommandExecutor<in S, in HCons<V, P>, out R>): Builder<S, P, V, R>

        fun build(): ArgumentCommandTree<S, P, V, R>
    }

    companion object {
        @JvmStatic
        fun <S, P : HList<P>, V, R> builder(): Builder<S, P, V, R> = SimpleArgumentCommandTree.Builder()
    }
}