package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList

/**
 * The state the bulk choose-lists sheet is derived from.
 *
 * [jacketBooks] is resolved here, off the composition, because `LibraryUiState.resolveSelectedBooks()`
 * scans every collected shelf. The scan runs ahead of `distinctUntilChanged`, so while selection mode
 * is active an unrelated change still re-runs it; deduping first would mean carrying
 * `booksByTab` / `bookByBookId` into the snapshot, whose structural comparison is the cost avoided.
 */
internal data class ChooseListsSnapshot(
    val selectedBookIds: Set<Int>,
    val customLists: List<BookList>,
    val listsBeingMutated: Set<Int>,
    val jacketBooks: List<Book>,
)
