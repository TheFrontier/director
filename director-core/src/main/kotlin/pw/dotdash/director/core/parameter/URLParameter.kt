@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.HList
import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL

fun url(): ValueParameter<Any?, HList<*>, URL> = URLParameter

private object URLParameter : ValueParameter<Any?, HList<*>, URL> {
    override fun parse(source: Any?, tokens: CommandTokens, previous: HList<*>): URL {
        val url: URL = try {
            URL(tokens.next())
        } catch (e: MalformedURLException) {
            throw tokens.createError("Invalid url.")
        }
        try {
            url.toURI()
        } catch (e: URISyntaxException) {
            throw tokens.createError("Invalid url.")
        }
        return url
    }
}