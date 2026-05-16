package nl.rhaydus.softcover.feature.personal.domain.usecase

import nl.rhaydus.softcover.feature.personal.domain.repository.ReadingSessionRepository

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
