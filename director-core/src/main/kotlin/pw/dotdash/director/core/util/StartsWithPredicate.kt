package pw.dotdash.director.core.util

class StartsWithPredicate(private val prefix: String): (String) -> Boolean {
    override fun invoke(test: String): Boolean =
        test.toLowerCase().startsWith(this.prefix.toLowerCase())
}