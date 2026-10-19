package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.RecentWindow
import kotlin.time.Instant

class ObserveRecentFetchedAtUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(window: RecentWindow): Flow<Instant?> =
        booksRepository.observeRecentFetchedAt(window = window)
}
