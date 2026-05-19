package nl.rhaydus.softcover.feature.personal.domain.usecase

import nl.rhaydus.softcover.feature.personal.domain.repository.ReadingSessionRepository

class StartReadingSessionUseCase(
    private val repository: ReadingSessionRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        startPage: Int? = null,
        startSeconds: Int? = null,
    ): Long = repository.start(
        bookId = bookId,
        startPage = startPage,
        startSeconds = startSeconds,
    )
}
