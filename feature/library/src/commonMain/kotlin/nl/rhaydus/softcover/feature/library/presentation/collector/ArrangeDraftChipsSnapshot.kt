package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryArrangeDraft
import nl.rhaydus.softcover.feature.library.presentation.state.buildLibraryArrangeChips

/**
 * The state slice the Arrange sheet's chip models are derived from. [compute] gates on
 * `distinctUntilChanged` upstream in `ArrangeDraftChipsCollector`, so its outputs
 * (`arrangeLayoutChips`, `arrangeSortChips`) are deliberately absent from this bundle. Mirrors
 * [FilterChipModelsSnapshot]'s shape.
 */
internal data class ArrangeDraftChipsSnapshot(
    val draft: LibraryArrangeDraft?,
    val visibleTabs: List<LibraryTab>,
    val customLists: List<BookList>,
    val booksByTab: Map<String, List<Book>>,
    val deadlines: Map<Int, BookDeadline>,
) {
    fun compute(): Pair<List<ChipUiModel>, List<ChipUiModel>> {
        val draft = draft ?: return emptyList<ChipUiModel>() to emptyList()

        return buildLibraryArrangeChips(
            draft = draft,
            visibleTabs = visibleTabs,
            customLists = customLists,
            booksByTab = booksByTab,
            deadlines = deadlines,
        )
    }
}
