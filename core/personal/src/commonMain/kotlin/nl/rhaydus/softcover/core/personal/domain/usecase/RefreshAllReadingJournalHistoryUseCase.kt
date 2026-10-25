package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingJournalHistoryRepository

class RefreshAllReadingJournalHistoryUseCase(
    private val repository: ReadingJournalHistoryRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatchingLogged {
        repository.refreshAllReadingJournalHistory()
    }
}
