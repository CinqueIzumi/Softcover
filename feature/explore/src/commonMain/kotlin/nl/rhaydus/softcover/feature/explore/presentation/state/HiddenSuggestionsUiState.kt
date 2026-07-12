package nl.rhaydus.softcover.feature.explore.presentation.state

import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.toad.UiState

internal data class HiddenSuggestionsUiState(
    val hiddenBooks: List<DismissedSeriesBook> = emptyList(),
    val hiddenSeries: List<DismissedSeries> = emptyList(),
    val initialized: Boolean = false,
) : UiState {
    val isEmpty: Boolean
        get() = initialized && hiddenBooks.isEmpty() && hiddenSeries.isEmpty()
}
