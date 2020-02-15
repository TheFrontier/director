package pw.dotdash.director.core.value

interface ValueUsage<in S> {

    fun getUsage(source: S, key: String): String
}