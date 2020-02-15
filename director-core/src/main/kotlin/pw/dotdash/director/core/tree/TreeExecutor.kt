package pw.dotdash.director.core.tree

import pw.dotdash.director.core.exception.TreeCommandException
import pw.dotdash.director.core.lexer.CommandTokens

interface TreeExecutor<in S, in V, out R> {

    @Throws(TreeCommandException::class)
    fun execute(source: S, tokens: CommandTokens, previous: V): R

    @Throws(TreeCommandException::class)
    fun complete(source: S, tokens: CommandTokens, previous: V): List<String>
}