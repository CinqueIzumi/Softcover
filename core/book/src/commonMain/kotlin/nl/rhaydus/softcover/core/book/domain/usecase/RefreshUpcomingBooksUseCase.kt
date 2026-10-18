package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import kotlin.time.Duration

class RefreshUpcomingBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        window: UpcomingWindow,
        maxAge: Duration,
    ): Result<Unit> = runCatchingLogged {
        booksRepository.refreshUpcomingBooks(
            window = window,
            maxAge = maxAge,
        )
    }
}
