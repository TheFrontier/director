package pw.dotdash.director.core.tree

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.HNil
import pw.dotdash.director.core.Parameter
import pw.dotdash.director.core.lexer.InputTokenizer
import pw.dotdash.director.core.tree.simple.SimpleRootCommandTree

interface RootCommandTree<in S, V : HList<V>, out R> : CommandTree<S, V, R> {

    val aliases: List<String>

    val initial: V

    val tokenizer: InputTokenizer

    val description: String?

    val extendedDescription: String?

    interface Builder<S, V : HList<V>, R> : CommandTree.Builder<S, V, R> {

        fun setAliases(aliases: Iterable<String>): Builder<S, V, R>

        fun setAliases(vararg aliases: String): Builder<S, V, R>

        fun setInitial(initial: V): Builder<S, V, R>

        fun setTokenizer(tokenizer: InputTokenizer): Builder<S, V, R>

        fun setDescription(description: String): Builder<S, V, R>

        fun setExtendedDescription(extendedDescription: String): Builder<S, V, R>

        override fun addChild(child: ChildCommandTree<S, V, R>): Builder<S, V, R>

        override fun addChild(aliases: List<String>, init: ChildCommandTree.Builder<S, V, R>.() -> Unit): Builder<S, V, R>

        override fun addChild(vararg aliases: String, init: ChildCommandTree.Builder<S, V, R>.() -> Unit): Builder<S, V, R>

        override fun setArgument(argument: ArgumentCommandTree<S, V, *, R>): Builder<S, V, R>

        override fun <NV> setArgument(parameter: Parameter<S, V, NV>, init: ArgumentCommandTree.Builder<S, V, NV, R>.() -> Unit): Builder<S, V, R>

        override fun setExecutor(executor: (S, V) -> R): Builder<S, V, R>

        fun build(): RootCommandTree<S, V, R>
    }

    companion object {
        @JvmStatic
        fun <S, R> builder(): Builder<S, HNil, R> =
            SimpleRootCommandTree.Builder()

        @JvmStatic
        fun <S, R> builder(aliases: List<String>): Builder<S, HNil, R> =
            SimpleRootCommandTree.Builder<S, HNil, R>().setAliases(aliases)

        @JvmStatic
        fun <S, R> builder(vararg aliases: String): Builder<S, HNil, R> =
            SimpleRootCommandTree.Builder<S, HNil, R>().setAliases(*aliases)

        @JvmStatic
        fun <S, V : HList<V>, R> builder(initial: V): Builder<S, V, R> =
            SimpleRootCommandTree.Builder<S, V, R>().setInitial(initial)

        @JvmStatic
        fun <S, V : HList<V>, R> builder(initial: V, aliases: List<String>): Builder<S, V, R> =
            SimpleRootCommandTree.Builder<S, V, R>().setInitial(initial).setAliases(aliases)

        @JvmStatic
        fun <S, V : HList<V>, R> builder(initial: V, vararg aliases: String): Builder<S, V, R> =
            SimpleRootCommandTree.Builder<S, V, R>().setInitial(initial).setAliases(*aliases)
    }
}