package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.toad.UiState
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.domain.model.LibraryGridLayout

data class LibraryUiState(
    val visibleTabs: List<LibraryTab> = listOf(
        LibraryTab.All,
        LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING),
    ),
    val selectedTabId: String = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id,
    val booksByTab: Map<String, List<Book>> = emptyMap(),
    val editionsByTab: Map<String, List<BookEdition>> = emptyMap(),

    val isLoading: Boolean = true,
    val gridLayout: LibraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
    val isLayoutMenuExpanded: Boolean = false,

    val deadlines: Map<Int, BookDeadline> = emptyMap(),
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
) : UiState
