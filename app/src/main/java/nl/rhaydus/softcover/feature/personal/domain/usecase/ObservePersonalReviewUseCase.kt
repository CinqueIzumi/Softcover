package nl.rhaydus.softcover.feature.personal.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.personal.domain.model.PersonalReview
import nl.rhaydus.softcover.feature.personal.domain.repository.PersonalReviewRepository

class ObservePersonalReviewUseCase(
    private val repository: PersonalReviewRepository,
) {
    operator fun invoke(bookId: Int): Flow<PersonalReview?> = repository.observe(bookId = bookId)
}
