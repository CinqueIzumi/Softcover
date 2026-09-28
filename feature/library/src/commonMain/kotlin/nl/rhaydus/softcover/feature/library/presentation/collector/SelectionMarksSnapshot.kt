package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout

/** The state fields the Library screen's per-book selection marks are derived from. */
internal data class SelectionMarksSnapshot(
    val selectionMode: Boolean,
    val selectedBookIds: Set<Int>,
    val booksByTab: Map<String, List<Book>>,
    val gridLayout: LibraryGridLayout,
)
