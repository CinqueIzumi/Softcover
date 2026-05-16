package nl.rhaydus.softcover.feature.personal.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.personal.domain.model.ReadingSession
import nl.rhaydus.softcover.feature.personal.domain.repository.ReadingSessionRepository

class ObserveSessionsForBookUseCase(
    private val repository: ReadingSessionRepository,
) {
    operator fun invoke(bookId: Int): Flow<List<ReadingSession>> =
        repository.observeByBookId(bookId = bookId)
}
