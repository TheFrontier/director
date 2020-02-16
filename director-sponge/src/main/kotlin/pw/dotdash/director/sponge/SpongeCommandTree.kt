package pw.dotdash.director.sponge

import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import pw.dotdash.director.core.HList
import pw.dotdash.director.core.HNil
import pw.dotdash.director.core.Parameter
import pw.dotdash.director.core.tree.ArgumentCommandTree
import pw.dotdash.director.core.tree.ChildCommandTree
import pw.dotdash.director.core.tree.CommandTree
import pw.dotdash.director.core.tree.RootCommandTree

object SpongeCommandTree {

    @JvmStatic
    fun <V : HList<V>> root(initial: V): RootCommandTree.Builder<CommandSource, V, CommandResult> =
        RootCommandTree.builder(initial)

    @JvmStatic
    fun root(): RootCommandTree.Builder<CommandSource, HNil, CommandResult> =
        RootCommandTree.builder(HNil)

    @JvmStatic
    fun root(aliases: List<String>): RootCommandTree.Builder<CommandSource, HNil, CommandResult> =
        RootCommandTree.builder(HNil, aliases)

    @JvmStatic
    fun root(vararg aliases: String): RootCommandTree.Builder<CommandSource, HNil, CommandResult> =
        RootCommandTree.builder(HNil, *aliases)

    @JvmStatic
    fun <P : HList<P>> child(): ChildCommandTree.Builder<CommandSource, P, CommandResult> =
        ChildCommandTree.builder()

    @JvmStatic
    fun <P : HList<P>> child(aliases: List<String>): ChildCommandTree.Builder<CommandSource, P, CommandResult> =
        ChildCommandTree.builder(aliases)

    @JvmStatic
    fun <P : HList<P>> child(vararg aliases: String): ChildCommandTree.Builder<CommandSource, P, CommandResult> =
        ChildCommandTree.builder(*aliases)

    @JvmStatic
    fun <P : HList<P>, V> argument(): ArgumentCommandTree.Builder<CommandSource, P, V, CommandResult> =
        ArgumentCommandTree.builder()

    @JvmStatic
    fun <P : HList<P>, V> argument(parameter: Parameter<CommandSource, P, V>): ArgumentCommandTree.Builder<CommandSource, P, V, CommandResult> =
        ArgumentCommandTree.builder(parameter)
}

fun <B : CommandTree.Builder<S, *, *>, S : CommandSource> B.setPermission(permission: String): B {
    this.setAccessibility { source, _ -> source.hasPermission(permission) }
    return this
}