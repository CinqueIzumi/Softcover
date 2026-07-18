package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingJournalHistoryRepository

class GetReadingJournalHistoryUseCase(
    private val readingJournalHistoryRepository: ReadingJournalHistoryRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<List<ReadingJournalEntry>> = runCatchingLogged {
        readingJournalHistoryRepository.getReadingJournalHistory(bookId = bookId)
    }
}
