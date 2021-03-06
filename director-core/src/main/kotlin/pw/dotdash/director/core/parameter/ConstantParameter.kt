@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.TransparentParameter
import pw.dotdash.director.core.value.ValueParameter

/**
 * Consumes no tokens, and returns the given constant value.
 *
 * @param value The value to return
 * @return The new value parameter
 */
fun <T> constant(value: T): ValueParameter<Any?, HList<*>, T> = ConstantParameter(value)

/**
 * Consumes no tokens, and returns [Unit].
 *
 * @return The new value parameter
 */
fun unit(): ValueParameter<Any?, HList<*>, Unit> = constant(Unit)

private class ConstantParameter<T>(val value: T) : ValueParameter<Any?, HList<*>, T>, TransparentParameter {
    override fun parse(source: Any?, tokens: CommandTokens, previous: HList<*>): T = this.value

    override fun getUsage(source: Any?, key: String): String = ""
}