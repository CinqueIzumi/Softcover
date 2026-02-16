package nl.rhaydus.softcover.feature.books.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class GetReadUserBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(): Flow<List<Book>> {
        return booksRepository.getBooksFlowByStatus(status = UserBookStatus.READ)
    }
}