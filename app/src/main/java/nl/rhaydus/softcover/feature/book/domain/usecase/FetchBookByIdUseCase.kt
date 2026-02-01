package nl.rhaydus.softcover.feature.book.domain.usecase

import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book.domain.repository.BookDetailRepository
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetAllUserBooksUseCase

class FetchBookByIdUseCase(
    private val bookDetailRepository: BookDetailRepository,
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
) {
    suspend operator fun invoke(id: Int): Result<Book> {
        return runCatching {
            val originalBook: Book = bookDetailRepository.fetchBookById(id = id)

            val userBooks: List<Book> = getAllUserBooksUseCase().firstOrNull()
                ?: return@runCatching originalBook

            userBooks.find { it.id == originalBook.id } ?: originalBook
        }
    }
}