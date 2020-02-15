package pw.dotdash.director.core.util

import java.util.*

fun <T : Any> Optional<T>.unwrap(): T? = this.orElse(null)