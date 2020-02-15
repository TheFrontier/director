@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter

/**
 * Abstract parameter that matches values based on pattern.
 */
abstract class PatternMatchingParameter<in S, in P : HList<@UnsafeVariance P>, out V : Any> : ValueParameter<S, P, Iterable<V>> {

    protected abstract fun getChoices(source: S, previous: P): Iterable<String>

    @Throws(IllegalArgumentException::class)
    protected abstract fun getValue(source: S, choice: String, previous: P): V

    protected fun getExact(source: S, choices: Iterable<String>, potential: String, previous: P): V? =
        choices.find { it.equals(potential, ignoreCase = true) }?.let { this.getValue(source, it, previous) }

    override fun parse(source: S, tokens: CommandTokens, previous: P): Iterable<V> {
        val unformattedRegex: String = tokens.next()
        val choices: Iterable<String> = getChoices(source, previous)

        val exact: V? = getExact(source, choices, unformattedRegex, previous)

        if (exact != null) return listOf(exact)

        val regex: Regex = formatRegex(unformattedRegex)
        val result: List<V> = choices.filterMap(regex::containsMatchIn) { this.getValue(source, it, previous) }

        if (result.isEmpty()) {
            throw tokens.createError("No values matching pattern '$unformattedRegex' found.")
        }

        return result
    }

    override fun complete(source: S, tokens: CommandTokens, previous: P): List<String> {
        val choices: Iterable<String> = getChoices(source, previous)
        val nextToken: String? = tokens.nextIfPresent()

        return if (nextToken != null) {
            val formatted: Regex = formatRegex(nextToken)
            choices.filter(formatted::containsMatchIn)
        } else {
            choices.toList()
        }
    }

    override fun getUsage(source: S, key: String): String = "<$key>"

    protected fun formatRegex(input: String): Regex =
        if (input.startsWith('^'))
            Regex(input, RegexOption.IGNORE_CASE)
        else
            Regex("^$input", RegexOption.IGNORE_CASE)

    private inline fun <T, R> Iterable<T>.filterMap(filter: (T) -> Boolean, map: (T) -> R): List<R> =
        this.filterMapTo(ArrayList(), filter, map)

    private inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.filterMapTo(destination: C, filter: (T) -> Boolean, map: (T) -> R): C {
        for (element: T in this) {
            if (filter(element)) {
                destination.add(map(element))
            }
        }
        return destination
    }
}