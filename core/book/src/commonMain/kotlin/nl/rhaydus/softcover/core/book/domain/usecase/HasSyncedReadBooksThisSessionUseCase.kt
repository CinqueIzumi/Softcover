package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository

class HasSyncedReadBooksThisSessionUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(): Boolean = booksRepository.hasSyncedReadBooksThisSession
}
