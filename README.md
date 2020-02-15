# director [![Release](https://jitpack.io/v/TheFrontier/director.svg)](https://jitpack.io/#TheFrontier/director)

A general-purpose, type-safe, tree-based command library.

## Gradle

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    // Just the core stuff.
    implementation("com.github.TheFrontier.director:director-core:<latest-version>")

    // Sponge Integration
    implementation("com.github.TheFrontier.director:director-sponge:<latest-version>")

    // COMING SOON: Velocity Integration
    implementation("com.github.TheFrontier.director:director-velocity:<latest-version>")

    // COMING SOON: Spigot Integration
    implementation("com.github.TheFrontier.director:director-spigot:<latest-version>")

    // COMING SOON: BungeeCord Integration
    implementation("com.github.TheFrontier.director:director-bungee:<latest-version>")
}
```

## A Long Example

The following example uses none of the shorthands available in the library. We quickly see that this becomes cumbersome.

```kotlin
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
import pw.dotdash.director.core.tree.ArgumentCommandTree
import pw.dotdash.director.core.tree.ChildCommandTree
import pw.dotdash.director.core.tree.RootCommandTree
import pw.dotdash.director.sponge.CommandTreeCallable
import pw.dotdash.director.sponge.parameter.player

fun longExample(plugin: Any) {
    fun warpCreate(): ChildCommandTree<CommandSource, HNil, CommandResult> {
        val executor: (CommandSource, HCons<String, HNil>) -> CommandResult =
            { source, args ->
                val (name: String) = args

                source.sendMessage(Text.of("Created warp: $name"))

                CommandResult.success()
            }

        val nameArgument =
            ArgumentCommandTree.builder<CommandSource, HNil, String, CommandResult>()
                .setParameter(string().key("name"))
                .setExecutor(executor)
                .build()

        return ChildCommandTree.builder<CommandSource, HNil, CommandResult>()
            .setAliases("create")
            .setArgument(nameArgument)
            .build()
    }

    fun warpTeleport(): ChildCommandTree<CommandSource, HNil, CommandResult> {
        val executor: (CommandSource, HCons<Player?, HCons<String, HNil>>) -> CommandResult =
            { source, args ->
                val (player: Player?, name: String) = args

                source.sendMessage(Text.of("${(player ?: source).name} teleported to warp $name"))

                CommandResult.success()
            }

        val playerArgument =
            ArgumentCommandTree.builder<CommandSource, HCons<String, HNil>, Player?, CommandResult>()
                .setParameter(player().optional().key("player"))
                .setExecutor(executor)
                .build()

        val nameArgument =
            ArgumentCommandTree.builder<CommandSource, HNil, String, CommandResult>()
                .setParameter(string().key("warp"))
                .setArgument(playerArgument)
                .build()

        return ChildCommandTree.builder<CommandSource, HNil, CommandResult>()
            .setAliases("create")
            .setArgument(nameArgument)
            .build()
    }

    val warp =
        RootCommandTree.builder<CommandSource, CommandResult>("warp")
            .addChild(warpCreate())
            .addChild(warpTeleport())
            .build()

    Sponge.getCommandManager().register(plugin, CommandTreeCallable(warp), "warp")
}
```

## A More Streamlined Example

By utilizing all the shorthands available in the library, we can greatly reduce the verbosity required to create a tree of commands.

```kotlin
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
```