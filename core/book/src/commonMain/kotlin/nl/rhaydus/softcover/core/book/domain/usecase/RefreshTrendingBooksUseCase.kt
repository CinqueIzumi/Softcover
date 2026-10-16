package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import kotlin.time.Duration

class RefreshTrendingBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        period: TrendingPeriod,
        maxAge: Duration,
    ): Result<Unit> = runCatchingLogged {
        booksRepository.refreshTrendingBooks(
            period = period,
            maxAge = maxAge,
        )
    }
}
