@file:JvmMultifileClass
@file:JvmName("SpongeValueParameters")

package pw.dotdash.director.sponge.parameter

import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.data.persistence.DataFormat
import org.spongepowered.api.data.persistence.DataFormats
import org.spongepowered.api.data.persistence.InvalidDataFormatException
import pw.dotdash.director.core.HList
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.parameter.string
import pw.dotdash.director.core.value.ValueParameter
import java.io.IOException

/**
 * Consumes tokens to output a [DataContainer].
 *
 * Acceptable inputs:
 * - A data format that is supported by the specified [format].
 *
 * @param format The data format to conform to
 * @param reader The value parameter used to read from input
 * @return The value parameter
 */
@JvmOverloads
fun <S, P : HList<P>> dataContainer(
    format: DataFormat = DataFormats.JSON,
    reader: ValueParameter<S, P, String> = string()
): ValueParameter<S, P, DataContainer> =
    DataContainerParameter(format, reader)

private data class DataContainerParameter<S, P : HList<P>>(
    val format: DataFormat,
    val reader: ValueParameter<S, P, String>
) : ValueParameter<S, P, DataContainer> {
    override fun parse(source: S, tokens: CommandTokens, previous: P): DataContainer {
        val data: String = this.reader.parse(source, tokens, previous)
        try {
            return this.format.readFrom(data.byteInputStream(Charsets.UTF_8))
        } catch (e: InvalidDataFormatException) {
            throw tokens.createError("Invalid data format.")
        } catch (e: IOException) {
            throw tokens.createError("Failed to read data: ${e.message ?: "reason unknown"}")
        }
    }

    override fun getUsage(source: S, key: String): String =
        this.reader.getUsage(source, key)
}