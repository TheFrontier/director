package pw.dotdash.director.sponge.test

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import pw.dotdash.director.core.HCons
import pw.dotdash.director.core.HNil
import pw.dotdash.director.core.component1
import pw.dotdash.director.core.component2
import pw.dotdash.director.core.parameter.optional
import pw.dotdash.director.core.parameter.string
import pw.dotdash.director.core.tree.RootCommandTree
import pw.dotdash.director.sponge.CommandTreeCallable
import pw.dotdash.director.sponge.SpongeCommandTree
import pw.dotdash.director.sponge.parameter.player

fun streamlinedExample(plugin: Any) {
    fun warpCreate(source: CommandSource, args: HCons<String, HNil>): CommandResult {
        val (name: String) = args

        source.sendMessage(Text.of("Created warp: $name"))

        return CommandResult.success()
    }

    fun warpTeleport(source: CommandSource, args: HCons<Player?, HCons<String, HNil>>): CommandResult {
        val (player: Player?, name: String) = args

        source.sendMessage(Text.of("${(player ?: source).name} teleported to warp $name"))

        return CommandResult.success()
    }

    val warp: RootCommandTree<CommandSource, HNil, CommandResult> =
        SpongeCommandTree.root("warp")
            .addChild("create") {
                setArgument(string() key "name") {
                    setExecutor(::warpCreate)
                }
            }
            .addChild("teleport") {
                setArgument(string() key "warp") {
                    setArgument(player().optional() key "player") {
                        setExecutor(::warpTeleport)
                    }
                }
            }
            .build()

    Sponge.getCommandManager().register(plugin, CommandTreeCallable(warp), "warp")
}