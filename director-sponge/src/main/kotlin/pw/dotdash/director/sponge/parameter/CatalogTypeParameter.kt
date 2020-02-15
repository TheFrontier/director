@file:JvmMultifileClass
@file:JvmName("SpongeValueParameters")

package pw.dotdash.director.sponge.parameter

import org.spongepowered.api.CatalogType
import org.spongepowered.api.Sponge
import org.spongepowered.api.world.DimensionType
import pw.dotdash.director.core.HList
import pw.dotdash.director.core.parameter.PatternMatchingParameter
import pw.dotdash.director.core.parameter.onlyOne
import pw.dotdash.director.core.value.ValueParameter
import kotlin.reflect.KClass

/**
 * Consumes tokens to output an [Iterable] of [T]s which are [CatalogType]s.
 *
 * Acceptable inputs:
 * - A catalog type's id
 * - A regex matching the beginning of at least one catalog type's id
 *
 * If you only want one catalog type, use [catalogType] or [onlyOne].
 *
 * @param type The type of the catalog type
 * @return The value parameter
 */
fun <T : CatalogType> catalogTypes(type: KClass<T>): ValueParameter<Any?, HList<*>, Iterable<T>> = CatalogTypeParameter(type.java)

/**
 * Consumes tokens to output a [T] which is a [CatalogType].
 *
 * Acceptable inputs:
 * - A catalog type's id
 * - A regex matching the beginning of a catalog type's id
 *
 * @param type The type of the catalog type
 * @return The value parameter
 */
fun <T : CatalogType> catalogType(type: KClass<T>): ValueParameter<Any?, HList<*>, T> = catalogTypes(type).onlyOne()

/**
 * Consumes tokens to output an [Iterable] of [T]s which are [CatalogType]s.
 *
 * Acceptable inputs:
 * - A catalog type's id
 * - A regex matching the beginning of at least one catalog type's id
 *
 * If you only want one catalog type, use [catalogType] or [onlyOne].
 *
 * @param T The type of the catalog type
 * @return The value parameter
 */
inline fun <reified T : CatalogType> catalogTypes(): ValueParameter<Any?, HList<*>, Iterable<T>> = catalogTypes(T::class)

/**
 * Consumes tokens to output a [T] which is a [CatalogType].
 *
 * Acceptable inputs:
 * - A catalog type's id
 * - A regex matching the beginning of a catalog type's id
 *
 * @param T The type of the catalog type
 * @return The value parameter
 */
inline fun <reified T : CatalogType> catalogType(): ValueParameter<Any?, HList<*>, T> = catalogTypes<T>().onlyOne()

private data class CatalogTypeParameter<T : CatalogType>(private val type: Class<T>) : PatternMatchingParameter<Any?, HList<*>, T>() {
    override fun getChoices(source: Any?, previous: HList<*>): Iterable<String> =
        Sponge.getRegistry().getAllOf(this.type).map(CatalogType::getId)

    override fun getValue(source: Any?, choice: String, previous: HList<*>): T =
        Sponge.getRegistry().getType(this.type, choice)
            .orElseThrow { IllegalArgumentException("Input value '$choice' wasn't a ${this.type.simpleName}") }
}