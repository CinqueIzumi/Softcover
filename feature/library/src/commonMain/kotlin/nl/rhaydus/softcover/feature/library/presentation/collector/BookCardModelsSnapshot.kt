package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

/** The state fields the Library grid's book and edition card models are derived from. */
internal data class BookCardModelsSnapshot(
    val visibleTabs: List<LibraryTab>,
    val booksByTab: Map<String, List<Book>>,
    val editionsByTab: Map<String, List<BookEdition>>,
    val gridLayout: LibraryGridLayout,
    val bookCovers: Map<Int, CoverUiModel>,
    val editionCovers: Map<Int, CoverUiModel>,
    val selectionMarks: Map<Int, SelectionMarkUiModel>,
    val deadlineCountdownOverlays: Map<Int, CoverOverlayUiModel>,
    val selectionMode: Boolean,
    val isRearranging: Boolean,
    val sortModeByTab: Map<String, LibrarySortMode>,
    val customLists: List<BookList>,
)
