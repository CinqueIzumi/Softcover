package nl.rhaydus.softcover.feature.library.presentation.state

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.PagerState
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.toad.UiState
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryStatusTab
import nl.rhaydus.softcover.feature.settings.domain.model.LibraryGridLayout

data class LibraryUiState(
    val allBooks: List<Book>? = null,
    val allBooksGridState: LazyGridState = LazyGridState(),

    val wantToReadBooks: List<Book>? = null,
    val wantToReadBooksGridState: LazyGridState = LazyGridState(),

    val currentlyReadingBooks: List<Book>? = null,
    val currentlyReadingBooksGridState: LazyGridState = LazyGridState(),

    val readBooks: List<Book>? = null,
    val readBooksGridState: LazyGridState = LazyGridState(),

    val dnfBooks: List<Book>? = null,
    val dnfBooksGridState: LazyGridState = LazyGridState(),

    val ownedEditions: List<BookEdition>? = null,
    val ownedEditionsGridState: LazyGridState = LazyGridState(),

    val isLoading: Boolean = true,
    val pagerState: PagerState = PagerState(
        currentPage = LibraryStatusTab.entries.indexOf(LibraryStatusTab.WANT_TO_READ),
        pageCount = { LibraryStatusTab.entries.size }
    ),
    val gridLayout: LibraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
    val isLayoutMenuExpanded: Boolean = false,
) : UiState