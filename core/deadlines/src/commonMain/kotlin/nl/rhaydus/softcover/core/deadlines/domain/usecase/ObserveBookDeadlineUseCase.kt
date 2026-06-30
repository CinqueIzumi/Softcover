package nl.rhaydus.softcover.core.deadlines.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.deadlines.domain.repository.BookDeadlineRepository
import nl.rhaydus.softcover.core.domain.model.BookDeadline

class ObserveBookDeadlineUseCase(
    private val repository: BookDeadlineRepository,
) {
    operator fun invoke(bookId: Int): Flow<BookDeadline?> = repository.observe(bookId = bookId)
}
