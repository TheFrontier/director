package pw.dotdash.director.core

import pw.dotdash.director.core.value.ValueParameter

data class Parameter<in S, in P : HList<@UnsafeVariance P>, out V>(val key: String, val value: ValueParameter<S, P, V>) {

    fun getUsage(source: S): String = this.value.getUsage(source, this.key)
}