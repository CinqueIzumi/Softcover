package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book

class ObserveUserBookUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(id: Int): Flow<Book?> = booksRepository.observeUserBook(id = id)
}
