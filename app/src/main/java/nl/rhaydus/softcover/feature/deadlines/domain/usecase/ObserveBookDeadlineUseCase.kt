package nl.rhaydus.softcover.feature.deadlines.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import nl.rhaydus.softcover.feature.deadlines.domain.repository.BookDeadlineRepository

class ObserveBookDeadlineUseCase(
    private val repository: BookDeadlineRepository,
) {
    operator fun invoke(bookId: Int): Flow<BookDeadline?> = repository.observe(bookId = bookId)
}
