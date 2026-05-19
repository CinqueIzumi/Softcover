package nl.rhaydus.softcover.feature.personal.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.personal.domain.model.ReadingLogEntry
import nl.rhaydus.softcover.feature.personal.domain.repository.ReadingLogRepository

class ObserveReadingLogForBookUseCase(
    private val repository: ReadingLogRepository,
) {
    operator fun invoke(bookId: Int): Flow<List<ReadingLogEntry>> =
        repository.observeByBookId(bookId = bookId)
}
