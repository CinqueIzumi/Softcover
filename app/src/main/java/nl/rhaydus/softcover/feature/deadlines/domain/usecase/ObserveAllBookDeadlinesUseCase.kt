package nl.rhaydus.softcover.feature.deadlines.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import nl.rhaydus.softcover.feature.deadlines.domain.repository.BookDeadlineRepository

class ObserveAllBookDeadlinesUseCase(
    private val repository: BookDeadlineRepository,
) {
    operator fun invoke(): Flow<Map<Int, BookDeadline>> =
        repository.observeAll().map { list -> list.associateBy { it.bookId } }
}
