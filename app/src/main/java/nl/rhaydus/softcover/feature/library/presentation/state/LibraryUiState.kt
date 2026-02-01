package nl.rhaydus.softcover.feature.library.presentation.state

import androidx.compose.foundation.pager.PagerState
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.UiState
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryStatusTab

data class LibraryUiState(
    val allBooks: List<Book> = emptyList(),
    val wantToReadBooks: List<Book> = emptyList(),
    val currentlyReadingBooks: List<Book> = emptyList(),
    val readBooks: List<Book> = emptyList(),
    val dnfBooks: List<Book> = emptyList(),
    val isLoading: Boolean = true,
    val pagerState: PagerState = PagerState(
        currentPage = LibraryStatusTab.entries.indexOf(LibraryStatusTab.WANT_TO_READ),
        pageCount = { LibraryStatusTab.entries.size }
    ),
) : UiState