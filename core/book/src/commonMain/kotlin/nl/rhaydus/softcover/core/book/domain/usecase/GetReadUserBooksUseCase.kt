package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus

class GetReadUserBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(): Flow<List<Book>> {
        return booksRepository.getBooksFlowByStatus(status = UserBookStatus.READ)
    }
}
