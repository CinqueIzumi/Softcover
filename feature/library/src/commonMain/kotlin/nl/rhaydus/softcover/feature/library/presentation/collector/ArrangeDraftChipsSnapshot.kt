package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
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
    val visibleTabs: ImmutableList<LibraryTab>,
    val customLists: ImmutableList<BookList>,
    val booksByTab: ImmutableMap<String, ImmutableList<Book>>,
    val deadlines: ImmutableMap<Int, BookDeadline>,
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
