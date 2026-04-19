package nl.rhaydus.softcover.feature.books.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class FetchBookByIdUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(id: Int): Result<Book> = runCatching {
        coroutineScope {
            val bookDeferred = async { booksRepository.fetchBookById(id = id) }
            val editionsDeferred = async { booksRepository.getEditionsByBookId(bookId = id) }

            val remoteBook = bookDeferred.await()
            val initialEditions = editionsDeferred.await()

            val remoteEditions = if (remoteBook.id != id) {
                booksRepository.getEditionsByBookId(bookId = remoteBook.id)
            } else {
                initialEditions
            }

            remoteBook.copy(editions = remoteEditions)
        }
    }
}
