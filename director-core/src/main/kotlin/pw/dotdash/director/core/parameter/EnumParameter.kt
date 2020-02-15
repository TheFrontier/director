@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.HList
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
fun <T : Enum<T>> enums(type: KClass<T>): ValueParameter<Any?, HList<*>, Iterable<T>> =
    EnumParameter(type.java)

/**
 * Consumes tokens to output a [T] which is a [Enum].
 *
 * Acceptable inputs:
 * - An enum's name
 * - A regex matching the beginning of a enum's name
 *
 * @param type The type of the enum
 * @return The value parameter
 */
fun <T : Enum<T>> enum(type: KClass<T>): ValueParameter<Any?, HList<*>, T> =
    enums(type).onlyOne()

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
inline fun <reified T : Enum<T>> enums(): ValueParameter<Any?, HList<*>, Iterable<T>> =
    enums(T::class)

/**
 * Consumes tokens to output a [T] which is a [Enum].
 *
 * Acceptable inputs:
 * - An enum's name
 * - A regex matching the beginning of a enum's name
 *
 * @param T The type of the enum
 * @return The value parameter
 */
inline fun <reified T : Enum<T>> enum(): ValueParameter<Any?, HList<*>, T> =
    enums<T>().onlyOne()

private data class EnumParameter<T : Enum<T>>(val type: Class<T>) : PatternMatchingParameter<Any?, HList<*>, T>() {
    private val values: Map<String, T> = this.type.enumConstants.associateBy(Enum<T>::name)

    override fun getChoices(source: Any?, previous: HList<*>): Iterable<String> =
        this.values.keys

    override fun getValue(source: Any?, choice: String, previous: HList<*>): T =
        this.values[choice] ?: throw IllegalArgumentException("Input value '$choice' wasn't a ${this.type.simpleName}")
}