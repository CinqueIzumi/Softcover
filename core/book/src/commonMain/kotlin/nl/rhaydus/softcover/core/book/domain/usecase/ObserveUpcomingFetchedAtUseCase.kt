package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import kotlin.time.Instant

class ObserveUpcomingFetchedAtUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(window: UpcomingWindow): Flow<Instant?> =
        booksRepository.observeUpcomingFetchedAt(window = window)
}
