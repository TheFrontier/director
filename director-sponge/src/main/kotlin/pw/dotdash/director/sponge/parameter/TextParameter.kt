@file:JvmMultifileClass
@file:JvmName("SpongeValueParameters")

package pw.dotdash.director.sponge.parameter

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColor
import org.spongepowered.api.text.serializer.TextParseException
import org.spongepowered.api.text.serializer.TextSerializer
import org.spongepowered.api.text.serializer.TextSerializers
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.parameter.onlyOne
import pw.dotdash.director.core.value.ValueParameter

/**
 * Consumes tokens to output a [Text].
 *
 * @receiver The value parameter used to read from input
 * @param serializer The serializer to parse the text with
 * @return The value parameter
 */
@JvmOverloads
fun <S, P> ValueParameter<S, P, String>.text(serializer: TextSerializer = TextSerializers.FORMATTING_CODE): ValueParameter<S, P, Text> =
    TextParameter(this, serializer)

private data class TextParameter<S, P>(
    val reader: ValueParameter<S, P, String>,
    val serializer: TextSerializer
) : ValueParameter<S, P, Text> {
    override fun parse(source: S, tokens: CommandTokens, previous: P): Text {
        val data = this.reader.parse(source, tokens, previous)
        try {
            return this.serializer.deserialize(data)
        } catch (e: TextParseException) {
            when {
                this.serializer == TextSerializers.JSON -> when (e.message) {
                    null -> throw tokens.createError("Invalid JSON text!")
                    else -> throw tokens.createError("Invalid JSON text: ${e.message}")
                }
                e.message == null -> throw tokens.createError("Invalid text!")
                else -> throw tokens.createError("Invalid text: ${e.message}")
            }
        }
    }

    override fun complete(source: S, tokens: CommandTokens, previous: P): List<String> =
        this.reader.complete(source, tokens, previous)

    override fun getUsage(source: S, key: String): String =
        this.reader.getUsage(source, key)
}

fun textColors(): ValueParameter<Any?, Any?, Iterable<TextColor>> = catalogTypes()

fun textColor(): ValueParameter<Any?, Any?, TextColor> = textColors().onlyOne()