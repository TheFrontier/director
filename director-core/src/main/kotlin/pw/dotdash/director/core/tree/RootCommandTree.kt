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

    interface Builder<S, V : HList<V>, R> : CommandTree.Builder<S, V, R> {

        fun aliases(aliases: Iterable<String>): Builder<S, V, R>

        fun aliases(vararg aliases: String): Builder<S, V, R>

        fun initial(initial: V): Builder<S, V, R>

        fun tokenizer(tokenizer: InputTokenizer): Builder<S, V, R>

        override fun addChild(aliases: List<String>, init: ChildCommandTree.Builder<S, V, R>.() -> Unit): Builder<S, V, R>

        override fun addChild(vararg aliases: String, init: ChildCommandTree.Builder<S, V, R>.() -> Unit): Builder<S, V, R>

        override fun <NV> argument(parameter: Parameter<S, V, NV>, init: ArgumentCommandTree.Builder<S, V, NV, R>.() -> Unit): Builder<S, V, R>

        override fun executor(executor: (S, V) -> R): Builder<S, V, R>

        override fun accessibility(test: (S, V) -> Boolean): Builder<S, V, R>

        override fun description(description: Any): Builder<S, V, R>

        override fun extendedDescription(extendedDescription: Any): Builder<S, V, R>

        fun build(): RootCommandTree<S, V, R>
    }

    companion object {
        @JvmStatic
        fun <S, R> builder(): Builder<S, HNil, R> =
            SimpleRootCommandTree.Builder()

        @JvmStatic
        fun <S, R> builder(aliases: List<String>): Builder<S, HNil, R> =
            SimpleRootCommandTree.Builder<S, HNil, R>().aliases(aliases)

        @JvmStatic
        fun <S, R> builder(vararg aliases: String): Builder<S, HNil, R> =
            SimpleRootCommandTree.Builder<S, HNil, R>().aliases(*aliases)

        @JvmStatic
        fun <S, V : HList<V>, R> builder(initial: V): Builder<S, V, R> =
            SimpleRootCommandTree.Builder<S, V, R>().initial(initial)

        @JvmStatic
        fun <S, V : HList<V>, R> builder(initial: V, aliases: List<String>): Builder<S, V, R> =
            SimpleRootCommandTree.Builder<S, V, R>().initial(initial).aliases(aliases)

        @JvmStatic
        fun <S, V : HList<V>, R> builder(initial: V, vararg aliases: String): Builder<S, V, R> =
            SimpleRootCommandTree.Builder<S, V, R>().initial(initial).aliases(*aliases)
    }
}