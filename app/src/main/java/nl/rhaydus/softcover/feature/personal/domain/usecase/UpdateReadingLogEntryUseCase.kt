package nl.rhaydus.softcover.feature.personal.domain.usecase

import nl.rhaydus.softcover.feature.personal.domain.model.ReadingLogEntry
import nl.rhaydus.softcover.feature.personal.domain.repository.ReadingLogRepository

class UpdateReadingLogEntryUseCase(
    private val repository: ReadingLogRepository,
) {
    suspend operator fun invoke(entry: ReadingLogEntry) = repository.update(entry = entry)
}
