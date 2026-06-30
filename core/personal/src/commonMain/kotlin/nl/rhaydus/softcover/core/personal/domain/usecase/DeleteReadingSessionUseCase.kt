package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.softcover.core.personal.domain.repository.ReadingSessionRepository

class DeleteReadingSessionUseCase(
    private val repository: ReadingSessionRepository,
) {
    suspend operator fun invoke(id: Long) = repository.delete(id = id)
}
