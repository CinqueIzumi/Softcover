package nl.rhaydus.softcover.feature.books.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class GetAllUserBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(): Flow<List<Book>> = booksRepository.books
}