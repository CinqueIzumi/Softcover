package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

/** The state fields the Library's book and edition card models are derived from. */
internal data class BookCardModelsSnapshot(
    val visibleTabs: List<LibraryTab>,
    val booksByTab: Map<String, List<Book>>,
    val editionsByTab: Map<String, List<BookEdition>>,
    val displayBooksByTab: Map<String, List<Book>>,
    val displayEditionsByTab: Map<String, List<BookEdition>>,
    val gridLayout: LibraryGridLayout,
    val deadlines: Map<Int, BookDeadline>,
    val dateStyle: DateStyle,
    val selectionMode: Boolean,
    val selectedBookIds: Set<Int>,
    val isRearranging: Boolean,
    val sortModeByTab: Map<String, LibrarySortMode>,
    val customLists: List<BookList>,
)
