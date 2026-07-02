package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.feature.library.presentation.sort.applyEditionSort
import nl.rhaydus.softcover.feature.library.presentation.util.finishedYear

internal fun computeDisplayBooks(
    raw: List<Book>,
    query: String,
    isReadTab: Boolean,
    selectedReadYear: Int?,
    filters: LibraryFilters,
): List<Book> {
    val q = query.trim()

    val searchFiltered = if (q.isEmpty()) {
        raw
    } else {
        raw.filter { book ->
            book.title.contains(
                q,
                ignoreCase = true,
            ) ||
                book.authors.any { it.name.contains(
                    q,
                    ignoreCase = true,
                ) }
        }
    }

    val yearFiltered = if (isReadTab && selectedReadYear != null) {
        searchFiltered.filter { it.finishedYear() == selectedReadYear }
    } else {
        searchFiltered
    }

    return if (filters.isEmpty) yearFiltered else yearFiltered.filter { filters.matchesBook(book = it) }
}

internal fun computeDisplayEditions(
    raw: List<BookEdition>,
    query: String,
    mode: LibrarySortMode,
    direction: SortDirection,
    addedAtByEditionId: Map<Int, String?>,
    filters: LibraryFilters,
    bookByBookId: Map<Int, Book>,
): List<BookEdition> {
    val q = query.trim()

    val searchFiltered = if (q.isEmpty()) {
        raw
    } else {
        raw.filter { edition ->
            edition.title.orEmpty().contains(
                q,
                ignoreCase = true,
            ) ||
                edition.authors.any { it.name.contains(
                    q,
                    ignoreCase = true,
                ) }
        }
    }

    val sorted = searchFiltered.applyEditionSort(
        mode = mode,
        direction = direction,
        addedAtByEditionId = addedAtByEditionId,
    )

    return if (filters.isEmpty) {
        sorted
    } else {
        sorted.filter { edition ->
            filters.matchesEdition(
                edition = edition,
                book = bookByBookId[edition.bookId],
            )
        }
    }
}
