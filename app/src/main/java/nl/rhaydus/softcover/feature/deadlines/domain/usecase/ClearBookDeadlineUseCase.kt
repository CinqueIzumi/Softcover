package nl.rhaydus.softcover.feature.deadlines.domain.usecase

import nl.rhaydus.softcover.feature.deadlines.domain.repository.BookDeadlineRepository

class ClearBookDeadlineUseCase(
    private val repository: BookDeadlineRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> = runCatching {
        repository.clearDeadline(bookId = bookId)
    }
}
