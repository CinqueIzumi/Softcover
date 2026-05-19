package nl.rhaydus.softcover.feature.personal.domain.usecase

import nl.rhaydus.softcover.feature.personal.domain.repository.PersonalReviewRepository

class SavePersonalReviewDraftUseCase(
    private val repository: PersonalReviewRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        body: String,
        hasSpoilers: Boolean,
    ) = repository.saveDraft(
        bookId = bookId,
        body = body,
        hasSpoilers = hasSpoilers,
    )
}
