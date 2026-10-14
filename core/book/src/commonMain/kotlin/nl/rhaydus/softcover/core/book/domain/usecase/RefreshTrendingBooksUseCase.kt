package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import kotlin.time.Duration

class RefreshTrendingBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(maxAge: Duration): Result<Unit> = runCatchingLogged {
        booksRepository.refreshTrendingBooks(maxAge = maxAge)
    }
}
