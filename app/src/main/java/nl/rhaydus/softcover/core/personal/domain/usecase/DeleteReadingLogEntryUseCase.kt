package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.softcover.core.personal.domain.repository.ReadingLogRepository

class DeleteReadingLogEntryUseCase(
    private val repository: ReadingLogRepository,
) {
    suspend operator fun invoke(id: Long) = repository.delete(id = id)
}
