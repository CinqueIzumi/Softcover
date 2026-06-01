package nl.rhaydus.softcover.feature.books.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class GetSortedBooksByStatusUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(
        status: UserBookStatus,
        mode: LibrarySortMode,
        direction: SortDirection,
    ): Flow<List<Book>> = booksRepository.getSortedBooksByStatus(
        status = status,
        mode = mode,
        direction = direction,
    )
}
