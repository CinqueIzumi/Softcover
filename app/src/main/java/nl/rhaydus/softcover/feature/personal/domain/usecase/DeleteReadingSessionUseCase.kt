package nl.rhaydus.softcover.feature.personal.domain.usecase

import nl.rhaydus.softcover.feature.personal.domain.repository.ReadingSessionRepository

class DeleteReadingSessionUseCase(
    private val repository: ReadingSessionRepository,
) {
    suspend operator fun invoke(id: Long) = repository.delete(id = id)
}
