@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Expect an argument to represent an integer within [range].
 */
fun int(range: ClosedRange<Int>): ValueParameter<Any?, Any?, Int> =
    NumberParameter(range, String::toIntOrNull)

/**
 * Expect an argument to represent an integer from −2,147,483,648 to 2,147,483,647.
 */
fun int(): ValueParameter<Any?, Any?, Int> =
    int(Int.MIN_VALUE..Int.MAX_VALUE)

/**
 * Expect an argument to represent an integer within [range].
 */
fun long(range: ClosedRange<Long>): ValueParameter<Any?, Any?, Long> =
    NumberParameter(range, String::toLongOrNull)

/**
 * Expect an argument to represent an integer from −9,223,372,036,854,775,808 to 9,223,372,036,854,775,807.
 */
fun long(): ValueParameter<Any?, Any?, Long> =
    long(Long.MIN_VALUE..Long.MAX_VALUE)

/**
 * Expect an argument to represent a single-precision floating point number within [range].
 */
fun float(range: ClosedRange<Float>): ValueParameter<Any?, Any?, Float> =
    NumberParameter(range, String::toFloatOrNull)

/**
 * Expect an argument to represent a single-precision floating point number from -[Float.MAX_VALUE] to [Float.MAX_VALUE].
 */
fun float(): ValueParameter<Any?, Any?, Float> =
    float(Float.MIN_VALUE..Float.MAX_VALUE)

/**
 * Expect an argument to represent a double-precision floating point number within [range].
 */
fun double(range: ClosedRange<Double>): ValueParameter<Any?, Any?, Double> =
    NumberParameter(range, String::toDoubleOrNull)

/**
 * Expect an argument to represent a double-precision floating point number from -[Double.MAX_VALUE] to [Double.MAX_VALUE].
 */
fun double(): ValueParameter<Any?, Any?, Double> =
    double(Double.MIN_VALUE..Double.MAX_VALUE)

private class NumberParameter<T : Comparable<T>>(val range: ClosedRange<T>, val fromString: (String) -> T?) : ValueParameter<Any?, Any?, T> {
    override fun parse(source: Any?, tokens: CommandTokens, previous: Any?): T {
        val parsed: T? = this.fromString(tokens.next())
        if (parsed == null || parsed !in this.range) {
            throw tokens.createError("Expected a number between ${this.range.start} and ${this.range.endInclusive}.")
        }
        return parsed
    }
}

/**
 * Expect an argument to represent an arbitrary-precision floating point number.
 */
fun bigDecimal(): ValueParameter<Any?, Any?, BigDecimal> =
    UnboundedNumberParameter(String::toBigDecimalOrNull)

/**
 * Expect an argument to represent an unbounded integer.
 */
fun bigInteger(): ValueParameter<Any?, Any?, BigInteger> =
    UnboundedNumberParameter(String::toBigIntegerOrNull)

private class UnboundedNumberParameter<T>(val fromString: (String) -> T?) : ValueParameter<Any?, Any?, T> {
    override fun parse(source: Any?, tokens: CommandTokens, previous: Any?): T =
        this.fromString(tokens.next()) ?: throw tokens.createError("Expected a number.")
}
