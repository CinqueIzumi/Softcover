package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.softcover.core.personal.domain.repository.ReadingSessionRepository

class StopReadingSessionUseCase(
    private val repository: ReadingSessionRepository,
) {
    suspend operator fun invoke(
        id: Long,
        endPage: Int? = null,
        endSeconds: Int? = null,
    ) = repository.stop(
        id = id,
        endPage = endPage,
        endSeconds = endSeconds,
    )
}
