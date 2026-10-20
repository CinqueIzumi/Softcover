package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import kotlin.time.Instant

class ObserveRecommendedFetchedAtUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(): Flow<Instant?> = booksRepository.observeRecommendedFetchedAt()
}
