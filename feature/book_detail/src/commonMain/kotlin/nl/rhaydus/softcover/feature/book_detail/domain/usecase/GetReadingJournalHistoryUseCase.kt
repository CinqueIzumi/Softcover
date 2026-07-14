package nl.rhaydus.softcover.feature.book_detail.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.feature.book_detail.domain.model.ReadingJournalEntry
import nl.rhaydus.softcover.feature.book_detail.domain.repository.ReadingJournalHistoryRepository

class GetReadingJournalHistoryUseCase(
    private val readingJournalHistoryRepository: ReadingJournalHistoryRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<List<ReadingJournalEntry>> = runCatchingLogged {
        readingJournalHistoryRepository.getReadingJournalHistory(bookId = bookId)
    }
}
