package nl.rhaydus.softcover.feature.book_detail.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.repository.UserTagVocabularyRepository

/**
 * Records tags the user just applied into the local vocabulary cache, so they surface as
 * suggestions on the next tagging pass without waiting for the next full [SyncUserTagVocabularyUseCase]
 * sync.
 */
class RecordAppliedTagsUseCase(
    private val userTagVocabularyRepository: UserTagVocabularyRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke(tags: List<UserTag>): Result<Unit> = runCatchingLogged {
        val userId = getUserIdUseCase().getOrThrow()

        userTagVocabularyRepository.record(
            userId = userId,
            tags = tags,
        )
    }
}
