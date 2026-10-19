package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.RecentWindow
import kotlin.time.Duration

class RefreshRecentBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        window: RecentWindow,
        maxAge: Duration,
    ): Result<Unit> = runCatchingLogged {
        booksRepository.refreshRecentBooks(
            window = window,
            maxAge = maxAge,
        )
    }
}
