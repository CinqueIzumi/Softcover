package nl.rhaydus.softcover.core.deadlines.domain.usecase

import nl.rhaydus.softcover.core.deadlines.domain.repository.BookDeadlineRepository
import nl.rhaydus.softcover.core.domain.result.runCatchingLogged

class ClearBookDeadlineUseCase(
    private val repository: BookDeadlineRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> = runCatchingLogged {
        repository.clearDeadline(bookId = bookId)
    }
}
