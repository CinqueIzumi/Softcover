package nl.rhaydus.softcover.feature.personal.domain.usecase

import nl.rhaydus.softcover.feature.personal.domain.repository.PersonalReviewRepository

class PublishPersonalReviewUseCase(
    private val repository: PersonalReviewRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        body: String,
        hasSpoilers: Boolean,
    ) = repository.publish(
        bookId = bookId,
        body = body,
        hasSpoilers = hasSpoilers,
    )
}
