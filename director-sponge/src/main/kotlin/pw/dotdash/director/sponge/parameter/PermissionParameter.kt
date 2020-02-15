@file:JvmMultifileClass
@file:JvmName("SpongeValueParameters")

package pw.dotdash.director.sponge.parameter

import org.spongepowered.api.service.permission.Subject
import pw.dotdash.director.core.HList
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter

/**
 * Checks the given [permission], to be able to use the given [parameter][this].
 * If the source doesn't have the permission, [ArgumentParseException] will be thrown.
 *
 * @receiver The parameter to wrap
 * @param permission The permission to check
 * @return The value parameter
 */
infix fun <S : Subject, P : HList<P>, V> ValueParameter<S, P, V>.permission(permission: String): ValueParameter<S, P, V> =
    PermissionParameter(this, permission)

private data class PermissionParameter<S : Subject, P : HList<P>, V>(
    val parameter: ValueParameter<S, P, V>,
    val permission: String
) : ValueParameter<S, P, V> {
    override fun parse(source: S, tokens: CommandTokens, previous: P): V {
        if (!source.hasPermission(this.permission)) {
            throw tokens.createError("You do not have permission to use this argument!")
        }
        return this.parameter.parse(source, tokens, previous)
    }

    override fun complete(source: S, tokens: CommandTokens, previous: P): List<String> =
        when (source.hasPermission(this.permission)) {
            true -> this.parameter.complete(source, tokens, previous)
            false -> emptyList()
        }

    override fun getUsage(source: S, key: String): String =
        this.parameter.getUsage(source, key)
}