package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

/**
 * The slice of [LibraryUiState] that the per-tab filter options derive from. [FilterOptionsCollector]
 * maps state to this bundle and gates recomputation with `distinctUntilChanged`, so the output
 * (`filterOptionsByTab`) is deliberately absent — a self-write can't retrigger the derivation.
 */
internal data class FilterOptionsInputs(
    val booksByTab: Map<String, List<Book>>,
    val editionsByTab: Map<String, List<BookEdition>>,
    val bookByBookId: Map<Int, Book>,
) {
    fun compute(): Map<String, LibraryFilterOptions> {
        val readTabId = LibraryTab.Status.of(UserBookStatus.READ).id

        val bookSide = booksByTab.mapValues { (tabId, books) ->
            buildBookFilterOptions(
                books = books,
                isReadTab = tabId == readTabId,
            )
        }

        val editionSide = editionsByTab.mapValues { (_, editions) ->
            buildEditionFilterOptions(
                editions = editions,
                bookByBookId = bookByBookId,
            )
        }

        return bookSide + editionSide
    }
}
