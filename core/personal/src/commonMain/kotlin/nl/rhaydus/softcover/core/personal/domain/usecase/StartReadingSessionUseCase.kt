package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.softcover.core.personal.domain.repository.ReadingSessionRepository

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
