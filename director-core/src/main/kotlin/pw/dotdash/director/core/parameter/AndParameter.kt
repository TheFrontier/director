@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.HCons
import pw.dotdash.director.core.HList
import pw.dotdash.director.core.HNil
import pw.dotdash.director.core.exception.ArgumentParseException
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter

infix fun <S, P : HList<P>, A, B> ValueParameter<S, P, A>.and(other: ValueParameter<S, P, B>): ValueParameter<S, P, HCons<A, HCons<B, HNil>>> =
    AndParameter(this, other)

private data class AndParameter<S, P : HList<P>, A, B>(
    val first: ValueParameter<S, P, A>,
    val second: ValueParameter<S, P, B>
) : ValueParameter<S, P, HCons<A, HCons<B, HNil>>> {

    override fun parse(source: S, tokens: CommandTokens, previous: P): HCons<A, HCons<B, HNil>> =
        HCons(this.first.parse(source, tokens, previous), HCons(this.second.parse(source, tokens, previous), HNil))

    override fun complete(source: S, tokens: CommandTokens, previous: P): List<String> {
        var snapshot: CommandTokens.Snapshot = tokens.snapshot
        try {
            this.first.parse(source, tokens, previous)
        } catch (e: ArgumentParseException) {
            tokens.snapshot = snapshot
            return this.first.complete(source, tokens, previous)
        }
        snapshot = tokens.snapshot
        try {
            this.second.parse(source, tokens, previous)
        } catch (e: ArgumentParseException) {
            tokens.snapshot = snapshot
            return this.second.complete(source, tokens, previous)
        }
        return emptyList()
    }

    override fun getUsage(source: S, key: String): String =
        "${this.first.getUsage(source, key)} ${this.second.getUsage(source, key)}"
}