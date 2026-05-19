package nl.rhaydus.softcover.feature.personal.domain.usecase

import nl.rhaydus.softcover.feature.personal.domain.repository.PersonalReviewRepository

class DeletePersonalReviewUseCase(
    private val repository: PersonalReviewRepository,
) {
    suspend operator fun invoke(bookId: Int) = repository.delete(bookId = bookId)
}
