@file:JvmMultifileClass
@file:JvmName("SpongeValueParameters")

package pw.dotdash.director.sponge.parameter

import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.source.RemoteSource
import org.spongepowered.api.entity.living.player.Player
import pw.dotdash.director.core.HList
import pw.dotdash.director.core.exception.ArgumentParseException
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter
import java.net.InetAddress
import java.net.UnknownHostException

fun ip(): ValueParameter<CommandSource, HList<*>, InetAddress> = ip(player(), false)

fun ipOrSource(): ValueParameter<CommandSource, HList<*>, InetAddress> = ip(player(), true)

@JvmOverloads
fun <S, P : HList<P>> ip(tryPlayer: ValueParameter<S, P, Player>, orSource: Boolean = false): ValueParameter<S, P, InetAddress> =
    IpParameter(tryPlayer, orSource)

private data class IpParameter<S, P : HList<P>>(
    val tryPlayer: ValueParameter<S, P, Player>,
    val orSource: Boolean
) : ValueParameter<S, P, InetAddress> {
    override fun parse(source: S, tokens: CommandTokens, previous: P): InetAddress {
        if (!tokens.hasNext() && this.orSource) {
            if (source is RemoteSource) {
                return source.connection.address.address
            } else {
                throw tokens.createError("No IP address was specified!")
            }
        }

        val snapshot: CommandTokens.Snapshot = tokens.snapshot
        val token: String = tokens.next()

        try {
            return InetAddress.getByName(token)
        } catch (e: UnknownHostException) {
            try {
                return tryPlayer.parse(source, tokens, previous).connection.address.address
            } catch (e: ArgumentParseException) {
                if (this.orSource && source is RemoteSource) {
                    tokens.snapshot = snapshot
                    return source.connection.address.address
                } else {
                    throw tokens.createError("Invalid IP address.")
                }
            }
        }
    }

    override fun getUsage(source: S, key: String): String =
        if (source is RemoteSource && this.orSource) "[<$key>]" else "<$key>"
}