package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.softcover.core.personal.domain.repository.ReadingSessionRepository

class ResumeReadingSessionUseCase(
    private val repository: ReadingSessionRepository,
) {
    suspend operator fun invoke(id: Long) = repository.resume(id = id)
}
