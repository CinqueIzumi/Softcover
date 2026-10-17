package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import kotlin.time.Instant

class ObserveTrendingFetchedAtUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(period: TrendingPeriod): Flow<Instant?> =
        booksRepository.observeTrendingFetchedAt(period = period)
}
