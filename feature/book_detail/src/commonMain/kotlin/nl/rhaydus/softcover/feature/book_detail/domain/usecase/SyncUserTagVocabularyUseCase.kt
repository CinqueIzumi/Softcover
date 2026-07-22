package nl.rhaydus.softcover.feature.book_detail.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.repository.UserTagVocabularyRepository

/**
 * Refreshes the local tag vocabulary cache from the user's full taggings history. The vocabulary is
 * keyed by the signed-in user, so the user id is resolved here rather than threaded through the
 * presentation layer.
 */
class SyncUserTagVocabularyUseCase(
    private val userTagVocabularyRepository: UserTagVocabularyRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingLogged {
        val userId = getUserIdUseCase().getOrThrow()

        userTagVocabularyRepository.syncFromRemote(userId = userId)
    }
}
