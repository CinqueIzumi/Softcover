package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.library.presentation.sort.applyEditionSort

internal fun computeDisplayBooks(
    raw: List<Book>,
    query: String,
    filters: LibraryFilters,
    userTagsByBookId: Map<Int, List<UserTag>>,
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
                book.authors.any {
                    it.name.contains(
                        q,
                        ignoreCase = true,
                    )
                }
        }
    }

    return if (filters.isEmpty) searchFiltered else searchFiltered.filter { book ->
        filters.matchesBook(
            book = book,
            userTags = userTagsByBookId[book.id].orEmpty(),
        )
    }
}

internal fun computeDisplayEditions(
    raw: List<BookEdition>,
    query: String,
    mode: LibrarySortMode,
    direction: SortDirection,
    addedAtByEditionId: Map<Int, String?>,
    filters: LibraryFilters,
    bookByBookId: Map<Int, Book>,
    userTagsByBookId: Map<Int, List<UserTag>>,
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
                edition.authors.any {
                    it.name.contains(
                        q,
                        ignoreCase = true,
                    )
                }
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
                userTags = userTagsByBookId[edition.bookId].orEmpty(),
            )
        }
    }
}

/**
 * The "Show N titles" preview count for the Filter sheet: how many of [tabId]'s items would be
 * visible under [draftFilters] (the sheet's uncommitted draft) plus the *current* search query —
 * sort/layout never affect this count, so the Arrange sheet doesn't need this helper and can use
 * the already-committed [LibraryUiState.tabStatsFor] instead. Reuses [computeDisplayBooks] /
 * [computeDisplayEditions] verbatim rather than re-implementing the search/filter predicates, so a
 * draft preview can never drift from what actually renders once committed via [OnApplyFiltersAction].
 */
internal fun libraryPreviewCount(
    state: LibraryUiState,
    tabId: String,
    draftFilters: LibraryFilters,
): Int {
    state.booksByTab[tabId]?.let { books ->
        return computeDisplayBooks(
            raw = books,
            query = state.searchQuery,
            filters = draftFilters,
            userTagsByBookId = state.userTagsByBookId,
        ).size
    }

    val editions = state.editionsByTab[tabId] ?: return 0

    return computeDisplayEditions(
        raw = editions,
        query = state.searchQuery,
        mode = state.sortModeFor(tabId = tabId),
        direction = state.sortDirectionFor(tabId = tabId),
        addedAtByEditionId = state.addedAtByTab[tabId].orEmpty(),
        filters = draftFilters,
        bookByBookId = state.bookByBookId,
        userTagsByBookId = state.userTagsByBookId,
    ).size
}
