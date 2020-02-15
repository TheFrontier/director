@file:JvmMultifileClass
@file:JvmName("SpongeValueParameters")

package pw.dotdash.director.sponge.parameter

import org.spongepowered.api.Sponge
import org.spongepowered.api.plugin.PluginContainer
import pw.dotdash.director.core.parameter.PatternMatchingParameter
import pw.dotdash.director.core.parameter.onlyOne
import pw.dotdash.director.core.value.ValueParameter

/**
 * Consumes tokens to output an [Iterable] of [PluginContainer]s.
 *
 * Acceptable inputs:
 * - A plugin's id
 * - A regex matching the beginning of at least one plugin's id
 *
 * If you only want one plugin, use [plugin] or [onlyOne].
 */
fun plugins(): ValueParameter<Any?, Any?, Iterable<PluginContainer>> = PluginContainerParameter

/**
 * Consumes tokens to output a [PluginContainer].
 *
 * Acceptable inputs:
 * - A plugin's id
 * - A regex matching the beginning of a plugin's id
 */
fun plugin(): ValueParameter<Any?, Any?, PluginContainer> = plugins().onlyOne()

private object PluginContainerParameter : PatternMatchingParameter<Any?, Any?, PluginContainer>() {
    override fun getChoices(source: Any?, previous: Any?): Iterable<String> =
        Sponge.getPluginManager().plugins.map(PluginContainer::getId)

    override fun getValue(source: Any?, choice: String, previous: Any?): PluginContainer =
        Sponge.getPluginManager().getPlugin(choice)
            .orElseThrow { IllegalArgumentException("Input value '$choice' wasn't a plugin") }

    override fun toString(): String = "PluginParameter"
}