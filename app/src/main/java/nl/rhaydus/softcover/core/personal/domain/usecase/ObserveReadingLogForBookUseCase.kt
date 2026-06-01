package nl.rhaydus.softcover.core.personal.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.personal.domain.model.ReadingLogEntry
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingLogRepository

class ObserveReadingLogForBookUseCase(
    private val repository: ReadingLogRepository,
) {
    operator fun invoke(bookId: Int): Flow<List<ReadingLogEntry>> =
        repository.observeByBookId(bookId = bookId)
}
