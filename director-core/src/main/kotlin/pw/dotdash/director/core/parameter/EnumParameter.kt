@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.value.ValueParameter
import kotlin.reflect.KClass

/**
 * Consumes tokens to output an [Iterable] of [T]s which are [Enum]s.
 *
 * Acceptable inputs:
 * - An enum's name
 * - A regex matching the beginning of at least one enum's name
 *
 * If you only want one enum, use [enumByName] or [onlyOne].
 *
 * @param type The type of the enum
 * @return The value parameter
 */
fun <T : Enum<T>> enums(type: KClass<T>): ValueParameter<Any?, Any?, Iterable<T>> =
    EnumParameter(type.java)

/**
 * Consumes tokens to output an [Iterable] of [T]s which are [Enum]s.
 *
 * Acceptable inputs:
 * - An enum's name
 * - A regex matching the beginning of at least one enum's name
 *
 * If you only want one enum, use [enumByName] or [onlyOne].
 *
 * @param T The type of the enum
 * @return The value parameter
 */
inline fun <reified T : Enum<T>> enums(): ValueParameter<Any?, Any?, Iterable<T>> =
    enums(T::class)

private data class EnumParameter<T : Enum<T>>(val type: Class<T>) : PatternMatchingParameter<Any?, Any?, T>() {
    private val values: Map<String, T> = this.type.enumConstants.associateBy(Enum<T>::name)

    override fun getChoices(source: Any?, previous: Any?): Iterable<String> =
        this.values.keys

    override fun getValue(source: Any?, choice: String, previous: Any?): T =
        this.values[choice] ?: throw IllegalArgumentException("Input value '$choice' wasn't a ${this.type.simpleName}")
}