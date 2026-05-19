package nl.rhaydus.softcover.feature.personal.domain.usecase

import nl.rhaydus.softcover.feature.personal.domain.repository.ReadingLogRepository

class DeleteReadingLogEntryUseCase(
    private val repository: ReadingLogRepository,
) {
    suspend operator fun invoke(id: Long) = repository.delete(id = id)
}
