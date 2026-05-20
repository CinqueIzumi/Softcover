package nl.rhaydus.softcover.feature.books.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
import nl.rhaydus.softcover.feature.settings.domain.model.SortDirection

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
