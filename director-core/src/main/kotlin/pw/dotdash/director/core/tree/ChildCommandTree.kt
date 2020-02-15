package pw.dotdash.director.core.tree

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.Parameter
import pw.dotdash.director.core.tree.simple.SimpleChildCommandTree

interface ChildCommandTree<in S, in P : HList<@UnsafeVariance P>, out R> : CommandTree<S, P, R> {

    val aliases: List<String>

    interface Builder<S, P : HList<P>, R> : CommandTree.Builder<S, P, R> {

        fun setAliases(aliases: Iterable<String>): Builder<S, P, R>

        fun setAliases(vararg aliases: String): Builder<S, P, R>

        override fun addChild(child: ChildCommandTree<S, P, R>): Builder<S, P, R>

        override fun addChild(aliases: List<String>, init: Builder<S, P, R>.() -> Unit): Builder<S, P, R>

        override fun addChild(vararg aliases: String, init: Builder<S, P, R>.() -> Unit): Builder<S, P, R>

        override fun setArgument(argument: ArgumentCommandTree<S, P, *, R>): Builder<S, P, R>

        override fun <NV> setArgument(parameter: Parameter<S, P, NV>, init: ArgumentCommandTree.Builder<S, P, NV, R>.() -> Unit): Builder<S, P, R>

        override fun setExecutor(executor: (S, P) -> R): Builder<S, P, R>

        fun build(): ChildCommandTree<S, P, R>
    }

    companion object {
        @JvmStatic
        fun <S, P : HList<P>, R> builder(): Builder<S, P, R> =
            SimpleChildCommandTree.Builder()

        @JvmStatic
        fun <S, P : HList<P>, R> builder(aliases: List<String>): Builder<S, P, R> =
            SimpleChildCommandTree.Builder<S, P, R>().setAliases(aliases)

        @JvmStatic
        fun <S, P : HList<P>, R> builder(vararg aliases: String): Builder<S, P, R> =
            SimpleChildCommandTree.Builder<S, P, R>().setAliases(*aliases)
    }
}