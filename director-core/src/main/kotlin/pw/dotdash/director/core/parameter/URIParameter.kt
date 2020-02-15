@file:JvmMultifileClass
@file:JvmName("ValueParameters")

package pw.dotdash.director.core.parameter

import pw.dotdash.director.core.lexer.CommandTokens
import pw.dotdash.director.core.value.ValueParameter
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

fun uri(): ValueParameter<Any?, Any?, URI> = URIParameter

private object URIParameter : ValueParameter<Any?, Any?, URI> {
    override fun parse(source: Any?, tokens: CommandTokens, previous: Any?): URI {
        val url: URL = try {
            URL(tokens.next())
        } catch (e: MalformedURLException) {
            throw tokens.createError("Invalid url.")
        }
        try {
            return url.toURI()
        } catch (e: URISyntaxException) {
            throw tokens.createError("Invalid url.")
        }
    }
}