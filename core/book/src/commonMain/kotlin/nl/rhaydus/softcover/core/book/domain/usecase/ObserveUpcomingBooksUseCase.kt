package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow

class ObserveUpcomingBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(window: UpcomingWindow): Flow<List<Book>> =
        booksRepository.observeUpcomingBooks(window = window)
}
