package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.book.domain.repository.ProgressHistoryRepository

class ObserveProgressUpdatesUseCase(
    private val progressHistoryRepository: ProgressHistoryRepository,
) {
    operator fun invoke(): Flow<List<ProgressUpdate>> = progressHistoryRepository.observeAllUpdates()
}
