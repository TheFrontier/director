@file:JvmMultifileClass
@file:JvmName("SpongeValueParameters")

package pw.dotdash.director.sponge.parameter

import org.spongepowered.api.Sponge
import org.spongepowered.api.service.permission.PermissionService
import org.spongepowered.api.service.permission.Subject
import org.spongepowered.api.service.permission.SubjectCollection
import pw.dotdash.director.core.HCons
import pw.dotdash.director.core.parameter.PatternMatchingParameter
import pw.dotdash.director.core.parameter.onlyOne
import pw.dotdash.director.core.value.ValueParameter

/**
 * Consumes tokens to output an [Iterable] of [SubjectCollection]s.
 *
 * Acceptable inputs:
 * - A subject collection's identifier
 * - A regex matching the beginning of at least one subject collection's identifier
 *
 * If you only want one subject collection, use [subjectCollection] or [onlyOne].
 */
fun subjectCollections(): ValueParameter<Any?, Any?, Iterable<SubjectCollection>> = SubjectCollectionParameter

/**
 * Consumes tokens to output a [SubjectCollection].
 *
 * Acceptable inputs:
 * - A subject collection's identifier
 * - A regex matching the beginning of a subject collection's identifier
 */
fun subjectCollection(): ValueParameter<Any?, Any?, SubjectCollection> = subjectCollections().onlyOne()

private object SubjectCollectionParameter : PatternMatchingParameter<Any?, Any?, SubjectCollection>() {
    override fun getChoices(source: Any?, previous: Any?): Iterable<String> =
        Sponge.getServiceManager().provideUnchecked(PermissionService::class.java).loadedCollections.keys

    override fun getValue(source: Any?, choice: String, previous: Any?): SubjectCollection =
        Sponge.getServiceManager().provideUnchecked(PermissionService::class.java).getCollection(choice)
            .orElseThrow { IllegalArgumentException("Input value '$choice' wasn't a subject collection") }

    override fun toString(): String = "SubjectCollectionParameter"
}

/**
 * Consumes tokens to output an [Iterable] of [Subject]s.
 * Requires a previous parameter of type [SubjectCollection].
 *
 * Acceptable inputs:
 * - A subject's identifier
 * - A regex matching the beginning of at least one subject's identifier
 *
 * If you only want one subject, use [subject] or [onlyOne].
 */
fun subjects(): ValueParameter<Any?, HCons<SubjectCollection, *>, Iterable<Subject>> = SubjectParameter

/**
 * Consumes tokens to output a [Subject].
 * Requires a previous parameter of type [SubjectCollection].
 *
 * Acceptable inputs:
 * - A subject's identifier
 * - A regex matching the beginning of a subject's identifier
 */
fun subject(): ValueParameter<Any?, HCons<SubjectCollection, *>, Subject> = subjects().onlyOne()

private object SubjectParameter : PatternMatchingParameter<Any?, HCons<SubjectCollection, *>, Subject>() {
    override fun getChoices(source: Any?, previous: HCons<SubjectCollection, *>): Iterable<String> =
        previous.head.loadedSubjects.map(Subject::getIdentifier)

    override fun getValue(source: Any?, choice: String, previous: HCons<SubjectCollection, *>): Subject =
        previous.head.getSubject(choice)
            .orElseThrow { IllegalArgumentException("Input value '$choice' wasn't a subject") }

    override fun toString(): String = "SubjectParameter"
}

/**
 * Consumes tokens to output an [Iterable] of [Subject]s.
 *
 * Acceptable inputs:
 * - A subject's identifier
 * - A regex matching the beginning of at least one subject's identifier
 *
 * If you only want one subject, use [subjectOf] or [onlyOne].
 *
 * @param collection The supplier of the subject collection to fetch from
 * @return The value parameter
 */
fun subjectsOf(collection: () -> SubjectCollection): ValueParameter<Any?, Any?, Iterable<Subject>> = SubjectOfParameter(collection)

/**
 * Consumes tokens to output a [Subject].
 *
 * Acceptable inputs:
 * - A subject's identifier
 * - A regex matching the beginning of a subject's identifier
 *
 * @param collection The supplier of the subject collection to fetch from
 * @return The value parameter
 */
fun subjectOf(collection: () -> SubjectCollection): ValueParameter<Any?, Any?, Subject> = subjectsOf(collection).onlyOne()

/**
 * Consumes tokens to output an [Iterable] of [Subject]s.
 *
 * Acceptable inputs:
 * - A subject's identifier
 * - A regex matching the beginning of at least one subject's identifier
 *
 * If you only want one subject, use [subjectOf] or [onlyOne].
 *
 * @param collectionId The identifier of the subject collection to fetch from
 * @return The value parameter
 */
fun subjectsOf(collectionId: String): ValueParameter<Any?, Any?, Iterable<Subject>> = subjectsOf {
    Sponge.getServiceManager().provideUnchecked(PermissionService::class.java).getCollection(collectionId)
        .orElseThrow { IllegalArgumentException("No subject collection found with name '$collectionId'") }
}

/**
 * Consumes tokens to output a [Subject].
 *
 * Acceptable inputs:
 * - A subject's identifier
 * - A regex matching the beginning of a subject's identifier
 *
 * @param collectionId The identifier of the subject collection to fetch from
 * @return The value parameter
 */
fun subjectOf(collectionId: String): ValueParameter<Any?, Any?, Subject> = subjectsOf(collectionId).onlyOne()

/**
 * Consumes tokens to output an [Iterable] of [Subject]s in the [PermissionService.SUBJECTS_USER] collection.
 *
 * Acceptable inputs:
 * - A subject's identifier
 * - A regex matching the beginning of at least one subject's identifier
 *
 * If you only want one subject, use [userSubject] or [onlyOne].
 */
fun userSubjects(): ValueParameter<Any?, Any?, Iterable<Subject>> = subjectsOf(PermissionService.SUBJECTS_USER)

/**
 * Consumes tokens to output an [Iterable] of [Subject]s in the [PermissionService.SUBJECTS_USER] collection.
 *
 * Acceptable inputs:
 * - A subject's identifier
 * - A regex matching the beginning of at least one subject's identifier
 */
fun userSubject(): ValueParameter<Any?, Any?, Subject> = subjectOf(PermissionService.SUBJECTS_USER)

/**
 * Consumes tokens to output an [Iterable] of [Subject]s in the [PermissionService.SUBJECTS_GROUP] collection.
 *
 * Acceptable inputs:
 * - A subject's identifier
 * - A regex matching the beginning of at least one subject's identifier
 *
 * If you only want one subject, use [groupSubject] or [onlyOne].
 */
fun groupSubjects(): ValueParameter<Any?, Any?, Iterable<Subject>> = subjectsOf(PermissionService.SUBJECTS_GROUP)

/**
 * Consumes tokens to output an [Iterable] of [Subject]s in the [PermissionService.SUBJECTS_GROUP] collection.
 *
 * Acceptable inputs:
 * - A subject's identifier
 * - A regex matching the beginning of at least one subject's identifier
 */
fun groupSubject(): ValueParameter<Any?, Any?, Subject> = subjectOf(PermissionService.SUBJECTS_GROUP)

private data class SubjectOfParameter(val collection: () -> SubjectCollection) : PatternMatchingParameter<Any?, Any?, Subject>() {
    override fun getChoices(source: Any?, previous: Any?): Iterable<String> =
        collection().loadedSubjects.map(Subject::getIdentifier)

    override fun getValue(source: Any?, choice: String, previous: Any?): Subject =
        collection().getSubject(choice)
            .orElseThrow { IllegalArgumentException("Input value '$choice' wasn't a subject") }
}