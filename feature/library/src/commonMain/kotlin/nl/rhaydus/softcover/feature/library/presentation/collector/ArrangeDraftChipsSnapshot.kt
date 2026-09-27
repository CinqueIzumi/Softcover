package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryArrangeChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryArrangeDraft
import nl.rhaydus.softcover.feature.library.presentation.state.buildLibraryArrangeChips

/**
 * The state slice the Arrange sheet's chip models are derived from. [compute] gates on
 * `distinctUntilChanged` upstream in `ArrangeDraftChipsCollector`, so its outputs
 * (`arrangeLayoutChips`, `arrangeSortChips`, `arrangeSortHeader`) are deliberately absent from this
 * bundle. Mirrors [FilterChipModelsSnapshot]'s shape.
 */
internal data class ArrangeDraftChipsSnapshot(
    val draft: LibraryArrangeDraft?,
    val visibleTabs: List<LibraryTab>,
    val customLists: List<BookList>,
    val booksByTab: Map<String, List<Book>>,
    val deadlines: Map<Int, BookDeadline>,
) {
    fun compute(): LibraryArrangeChips {
        val draft = draft
            ?: return LibraryArrangeChips.Empty

        return buildLibraryArrangeChips(
            draft = draft,
            visibleTabs = visibleTabs,
            customLists = customLists,
            booksByTab = booksByTab,
            deadlines = deadlines,
        )
    }
}
