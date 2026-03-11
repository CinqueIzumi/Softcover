package nl.rhaydus.softcover.feature.books.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class GetAllUserListsUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(): Flow<List<BookList>> = booksRepository.allUserLists
}