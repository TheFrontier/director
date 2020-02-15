package pw.dotdash.director.core.tree

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.tree.simple.SimpleChildCommandTree

interface ChildCommandTree<in S, in P : HList<@UnsafeVariance P>, out R> : CommandTree<S, P, R> {

    val aliases: List<String>

    interface Builder<S, P : HList<P>, R> : CommandTree.Builder<S, P, R> {

        fun setAliases(aliases: Iterable<String>): Builder<S, P, R>

        fun setAliases(vararg aliases: String): Builder<S, P, R>

        override fun addChild(child: ChildCommandTree<S, P, R>): Builder<S, P, R>

        override fun setArgument(argument: ArgumentCommandTree<S, P, *, R>): Builder<S, P, R>

        override fun setExecutor(executor: CommandExecutor<in S, in P, out R>): Builder<S, P, R>

        fun build(): ChildCommandTree<S, P, R>
    }

    companion object {
        @JvmStatic
        fun <S, P : HList<P>, R> builder(): Builder<S, P, R> = SimpleChildCommandTree.Builder()
    }
}