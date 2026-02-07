package nl.rhaydus.softcover.feature.library.presentation.state

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.PagerState
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.UiState
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryStatusTab

data class LibraryUiState(
    val allBooks: List<Book> = emptyList(),
    val allBooksGridState: LazyGridState = LazyGridState(),

    val wantToReadBooks: List<Book> = emptyList(),
    val wantToReadBooksGridState: LazyGridState = LazyGridState(),

    val currentlyReadingBooks: List<Book> = emptyList(),
    val currentlyReadingBooksGridState: LazyGridState = LazyGridState(),

    val readBooks: List<Book> = emptyList(),
    val readBooksGridState: LazyGridState = LazyGridState(),

    val dnfBooks: List<Book> = emptyList(),
    val dnfBooksGridState: LazyGridState = LazyGridState(),

    val isLoading: Boolean = true,
    val pagerState: PagerState = PagerState(
        currentPage = LibraryStatusTab.entries.indexOf(LibraryStatusTab.WANT_TO_READ),
        pageCount = { LibraryStatusTab.entries.size }
    ),
) : UiState