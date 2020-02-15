package pw.dotdash.director.sponge.parameter

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.entity.living.player.User
import org.spongepowered.api.service.user.UserStorageService
import pw.dotdash.director.core.parameter.onlyOne
import pw.dotdash.director.core.parameter.orSource
import pw.dotdash.director.core.util.unwrap
import pw.dotdash.director.core.value.ValueParameter

/**
 * Consumes tokens to output an [Iterable] of [User]s.
 *
 * Acceptable inputs:
 * - A user's username
 * - A regex matching the beginning of at least one user's username
 * - A selector
 *
 * If you only want one user, use [user] or [onlyOne].
 * If you want to also allow the source, use [userOrSource] or [orSource].
 */
fun users(): ValueParameter<CommandSource, Any?, Iterable<User>> = UserParameter

/**
 * Consumes tokens to output a [User].
 *
 * Acceptable inputs:
 * - A user's username
 * - A regex matching the beginning of a user's username
 * - A selector
 *
 * If you want to also allow the source, use [userOrSource] or [orSource].
 */
fun user(): ValueParameter<CommandSource, Any?, User> = users().onlyOne()

/**
 * Consumes tokens to output a [User].
 *
 * Acceptable inputs:
 * - A user's username
 * - A regex matching the beginning of a user's username
 * - A selector
 *
 * If failed, will try to use the source as the output.
 */
fun userOrSource(): ValueParameter<CommandSource, Any?, User> = user().orSource()

private object UserParameter : SelectorParameter<Any?, User>(User::class) {
    override fun getChoices(source: CommandSource, previous: Any?): Iterable<String> =
        Sponge.getServiceManager().provideUnchecked(UserStorageService::class.java).all.asSequence()
            .take(500)
            .mapNotNull { it.name.unwrap() }
            .toList()

    override fun getValue(source: CommandSource, choice: String, previous: Any?): User =
        Sponge.getServiceManager().provideUnchecked(UserStorageService::class.java)[choice]
            .orElseThrow { IllegalArgumentException("Input value '$choice' was not a user") }

    override fun toString(): String = "UserParameter"
}