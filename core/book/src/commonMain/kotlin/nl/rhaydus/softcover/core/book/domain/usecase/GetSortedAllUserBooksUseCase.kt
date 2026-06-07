package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection

class GetSortedAllUserBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(
        mode: LibrarySortMode,
        direction: SortDirection,
    ): Flow<List<Book>> = booksRepository.getSortedAllUserBooks(
        mode = mode,
        direction = direction,
    )
}
