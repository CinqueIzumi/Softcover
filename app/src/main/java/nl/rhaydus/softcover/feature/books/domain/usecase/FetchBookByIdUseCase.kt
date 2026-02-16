package nl.rhaydus.softcover.feature.books.domain.usecase

import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class FetchBookByIdUseCase(
    private val booksRepository: BooksRepository,
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
) {
    suspend operator fun invoke(id: Int): Result<Book> {
        return runCatching {
            val originalBook: Book = booksRepository.fetchBookById(id = id)

            val userBooks: List<Book> = getAllUserBooksUseCase().firstOrNull()
                ?: return@runCatching originalBook

            userBooks.find { it.id == originalBook.id } ?: originalBook
        }
    }
}