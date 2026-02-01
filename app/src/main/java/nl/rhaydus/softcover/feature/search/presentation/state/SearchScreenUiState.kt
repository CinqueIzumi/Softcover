package nl.rhaydus.softcover.feature.search.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.UiState

data class SearchScreenUiState(
    val previousSearchQueries: List<String> = emptyList(),
    val queriedBooks: List<Book> = emptyList(),
    val searchText: String = "",
    val isLoading: Boolean = false,
) : UiState