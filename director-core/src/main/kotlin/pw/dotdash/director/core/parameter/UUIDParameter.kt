@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter
import java.util.*

/**
 * Expect an argument to represent a valid [UUID] (i.e. 0f58a5fd-20d1-4e18-8c6c-20e30f074209).
 */
fun uuid(): ValueParameter<Any?, Any?, UUID> = UUIDParameter

private object UUIDParameter : ValueParameter<Any?, Any?, UUID> {
    override fun parse(source: Any?, tokens: CommandTokens, previous: Any?): UUID =
        try {
            UUID.fromString(tokens.next())
        } catch (e: IllegalArgumentException) {
            throw tokens.createError("Invalid UUID. Must be in the format \"xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx\".")
        }
}