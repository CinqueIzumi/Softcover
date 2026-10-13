package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.lists.ChooseListsUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.core.uibinding.lists.toBulkChooseListsUiModel
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

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

internal fun LibraryUiState.chooseListsSnapshot(): ChooseListsSnapshot = ChooseListsSnapshot(
    selectedBookIds = selectedBookIds,
    customLists = customLists,
    listsBeingMutated = listsBeingMutated,
    jacketBooks = resolveSelectedBooks().take(SheetHeaderTrailing.JacketStack.MAX_COVERS),
)

internal fun ChooseListsSnapshot.toUiModel(previous: ChooseListsUiModel?): ChooseListsUiModel? {
    if (selectedBookIds.isEmpty()) return null

    val covers = jacketBooks.map { book -> book.toCoverUiModel(variant = CoverVariant.StackedJacket) }

    return customLists.toBulkChooseListsUiModel(
        bookIds = selectedBookIds,
        covers = covers,
        listsBeingMutated = listsBeingMutated,
        previous = previous,
    )
}
