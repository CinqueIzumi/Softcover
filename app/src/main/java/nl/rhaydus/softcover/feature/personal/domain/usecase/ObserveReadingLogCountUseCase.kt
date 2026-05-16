package nl.rhaydus.softcover.feature.personal.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.personal.domain.repository.ReadingLogRepository

class ObserveReadingLogCountUseCase(
    private val repository: ReadingLogRepository,
) {
    operator fun invoke(bookId: Int): Flow<Int> =
        repository.observeCountByBookId(bookId = bookId)
}
