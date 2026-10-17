package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingJournalHistoryRepository

class RefreshReadingJournalHistoryUseCase(
    private val repository: ReadingJournalHistoryRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> = runCatchingLogged {
        repository.refreshReadingJournalHistory(bookId = bookId)
    }
}
