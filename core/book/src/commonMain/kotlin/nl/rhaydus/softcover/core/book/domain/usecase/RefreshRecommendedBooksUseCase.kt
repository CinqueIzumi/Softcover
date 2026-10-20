package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import kotlin.time.Duration

class RefreshRecommendedBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(maxAge: Duration): Result<Unit> = runCatchingLogged {
        booksRepository.refreshRecommendedBooks(maxAge = maxAge)
    }
}
