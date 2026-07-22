package nl.rhaydus.softcover.feature.book_detail.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.repository.UserTagVocabularyRepository

/**
 * Observes the current user's cached tag vocabulary for suggestion surfaces. Falls back to an empty
 * list rather than propagating a failure — a suggestion collector should never crash a screen
 * because the user id couldn't be resolved or the local read failed.
 */
class ObserveUserTagVocabularyUseCase(
    private val userTagVocabularyRepository: UserTagVocabularyRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    operator fun invoke(): Flow<List<UserTag>> = flow {
        val userId = getUserIdUseCase().getOrThrow()

        emitAll(userTagVocabularyRepository.observe(userId = userId))
    }.catch { emit(emptyList()) }
}
