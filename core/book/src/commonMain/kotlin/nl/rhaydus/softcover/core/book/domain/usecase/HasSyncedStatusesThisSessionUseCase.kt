package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.UserBookStatus

class HasSyncedStatusesThisSessionUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(statuses: Set<UserBookStatus>): Boolean =
        booksRepository.syncedStatusesThisSession.containsAll(statuses)
}
