package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.softcover.core.personal.domain.model.ReadingLogEntry
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingLogRepository

class UpdateReadingLogEntryUseCase(
    private val repository: ReadingLogRepository,
) {
    suspend operator fun invoke(entry: ReadingLogEntry) = repository.update(entry = entry)
}
