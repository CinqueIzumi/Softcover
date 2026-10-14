package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import kotlin.time.Instant

class ObserveTrendingFetchedAtUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(): Flow<Instant?> = booksRepository.observeTrendingFetchedAt()
}
