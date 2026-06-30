package nl.rhaydus.softcover.feature.explore.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.toad.UiState

internal data class ExploreScreenUiState(
    val previousSearchQueries: List<String> = emptyList(),
    val queriedBooks: List<Book> = emptyList(),
    val trendingBooks: List<Book> = emptyList(),
    val loadingTrendingBooks: Boolean = true,
    val continueSeriesBooks: List<Book> = emptyList(),
    val loadingContinueSeriesBooks: Boolean = true,
    val searchText: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchError: String? = null,
) : UiState
