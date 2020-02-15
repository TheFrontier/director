package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter

fun boolean(map: Map<String, Boolean>): ValueParameter<Any?, HList<*>, Boolean> =
    choices(map)

private val BOOLEAN_CHOICES = mapOf(
    "true" to true, "t" to true, "yes" to true, "y" to true, "1" to true,
    "false" to false, "f" to false, "no" to false, "n" to false, "0" to false
)

fun boolean(): ValueParameter<Any?, HList<*>, Boolean> =
    boolean(BOOLEAN_CHOICES)

fun <T : Any> choices(keys: () -> Iterable<String>, get: (String) -> T?): ValueParameter<Any?, HList<*>, T> =
    ChoicesParameter(keys, get)

fun <T : Any> choices(map: Map<String, T>): ValueParameter<Any?, HList<*>, T> =
    choices(map::keys, map::get)

@JvmName("choicesAny")
fun <T : Any> choices(map: Map<out Any, T>): ValueParameter<Any?, HList<*>, T> =
    map.mapKeys { (key, _) -> key.toString() }.let { choices(it::keys, it::get) }

private class ChoicesParameter<T : Any>(val keys: () -> Iterable<String>, val get: (String) -> T?) : ValueParameter<Any?, HList<*>, T> {
    override fun parse(source: Any?, tokens: CommandTokens, previous: HList<*>): T =
        this.get(tokens.next()) ?: throw tokens.createError("Unknown value. Valid choices: ${this.keys().joinToString()}")

    override fun complete(source: Any?, tokens: CommandTokens, previous: HList<*>): List<String> {
        val prefix = tokens.nextIfPresent().orEmpty()
        return this.keys().filter(prefix.startsWith())
    }
}

fun <T : Any> choicesCached(keys: () -> Iterable<String>, get: (String) -> T?): ValueParameter<Any?, HList<*>, T> =
    CachedChoicesParameter(keys, get)

fun <T : Any> choicesCached(map: Map<String, T>): ValueParameter<Any?, HList<*>, T> =
    choicesCached(map::keys, map::get)

@JvmName("choicesCachedAny")
fun <T : Any> choicesCached(map: Map<out Any, T>): ValueParameter<Any?, HList<*>, T> =
    map.mapKeys { (key, _) -> key.toString() }.let { choicesCached(it::keys, it::get) }

private class CachedChoicesParameter<T : Any>(keys: () -> Iterable<String>, val get: (String) -> T?) : ValueParameter<Any?, HList<*>, T> {
    private val keys by lazy(keys)

    override fun parse(source: Any?, tokens: CommandTokens, previous: HList<*>): T =
        this.get(tokens.next()) ?: throw tokens.createError("Unknown value. Valid choices: ${this.keys.joinToString()}")

    override fun complete(source: Any?, tokens: CommandTokens, previous: HList<*>): List<String> {
        val prefix: String = tokens.nextIfPresent().orEmpty()
        return this.keys.filter(prefix.startsWith())
    }
}

private fun String.startsWith(): (String) -> Boolean = {
    it.toLowerCase().startsWith(this.toLowerCase())
}