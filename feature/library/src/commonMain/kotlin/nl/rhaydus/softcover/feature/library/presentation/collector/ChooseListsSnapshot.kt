package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList

/**
 * The state the bulk choose-lists sheet is derived from.
 *
 * [jacketEditions] is already resolved: the selection's covers are looked up here, off the
 * composition, rather than in the render — `LibraryUiState.resolveSelectedBooks()` scans every
 * collected shelf, and both layouts used to call it on every frame the sheet was open.
 *
 * **What that does and does not buy.** The scan is now per *state emission* rather than per frame,
 * and it short-circuits entirely while nothing is selected. It is not, however, behind
 * `distinctUntilChanged` — that dedupes this snapshot, so while selection mode is active an
 * unrelated change (a search keystroke, a sort, a filter) still re-runs the scan even though its
 * result is about to be discarded as a duplicate. Left as is deliberately: moving it behind the
 * dedupe means either carrying `booksByTab`/`bookByBookId` into the snapshot (whose own structural
 * comparison is the cost being avoided) or reimplementing `resolveSelectedBooks()` here, away from
 * the state it belongs to. Selection mode is transient and the frame-rate problem is the one that
 * mattered.
 */
internal data class ChooseListsSnapshot(
    val selectedBookIds: Set<Int>,
    val customLists: List<BookList>,
    val listsBeingMutated: Set<Int>,
    val jacketEditions: List<BookEdition>,
)
