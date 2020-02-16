@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.TransparentParameter
import pw.dotdash.director.core.value.ValueParameter

/**
 * Consumes no tokens, and returns the source.
 *
 * @return The new value parameter
 */
fun <S> source(): ValueParameter<S, HList<*>, S> = @Suppress("UNCHECKED_CAST") (SourceParameter as ValueParameter<S, HList<*>, S>)

private object SourceParameter : ValueParameter<Any?, HList<*>, Any?>, TransparentParameter {
    override fun parse(source: Any?, tokens: CommandTokens, previous: HList<*>): Any? = source

    override fun getUsage(source: Any?, key: String): String = ""
}