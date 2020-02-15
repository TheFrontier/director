package pw.dotdash.director.sponge.parameter

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.entity.living.player.Player
import pw.dotdash.director.core.parameter.onlyOne
import pw.dotdash.director.core.parameter.orSource
import pw.dotdash.director.core.value.ValueParameter

/**
 * Consumes tokens to output an [Iterable] of [Player]s.
 *
 * Acceptable inputs:
 * - A player's username
 * - A regex matching the beginning of at least one player's username
 * - A selector
 *
 * If you only want one player, use [player] or [onlyOne].
 * If you want to also allow the source, use [playerOrSource] or [orSource].
 */
fun players(): ValueParameter<CommandSource, Any?, Iterable<Player>> = PlayerParameter

/**
 * Consumes tokens to output a [Player].
 *
 * Acceptable inputs:
 * - A player's username
 * - A regex matching the beginning of a player's username
 * - A selector
 *
 * If you want to also allow the source, use [playerOrSource] or [orSource].
 */
fun player(): ValueParameter<CommandSource, Any?, Player> = players().onlyOne()

/**
 * Consumes tokens to output a [Player].
 *
 * Acceptable inputs:
 * - A player's username
 * - A regex matching the beginning of a player's username
 * - A selector
 *
 * If failed, will try to use the source as the output.
 */
fun playerOrSource(): ValueParameter<CommandSource, Any?, Player> = player().orSource()

private object PlayerParameter : SelectorParameter<Any?, Player>(Player::class) {
    override fun getChoices(source: CommandSource, previous: Any?): Iterable<String> =
        Sponge.getServer().onlinePlayers.map(Player::getName)

    override fun getValue(source: CommandSource, choice: String, previous: Any?): Player =
        Sponge.getServer().getPlayer(choice)
            .orElseThrow { IllegalArgumentException("Input value '$choice' wasn't a player") }

    override fun toString(): String = "PlayerParameter"
}