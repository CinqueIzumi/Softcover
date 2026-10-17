package nl.rhaydus.softcover.core.personal.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingJournalHistoryRepository

class ObserveReadingJournalHistoryUseCase(
    private val repository: ReadingJournalHistoryRepository,
) {
    operator fun invoke(bookId: Int): Flow<List<ReadingJournalEntry>> =
        repository.observeReadingJournalHistory(bookId = bookId)
}
