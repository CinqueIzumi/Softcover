package nl.rhaydus.softcover.core.personal.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.ReadingSession
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingSessionRepository

class ObserveSessionsForBookUseCase(
    private val repository: ReadingSessionRepository,
) {
    operator fun invoke(bookId: Int): Flow<List<ReadingSession>> =
        repository.observeByBookId(bookId = bookId)
}
