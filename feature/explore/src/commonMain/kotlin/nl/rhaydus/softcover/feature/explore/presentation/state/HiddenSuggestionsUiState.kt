package nl.rhaydus.softcover.feature.explore.presentation.state

import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.toad.UiState

internal data class HiddenSuggestionsUiState(
    val hiddenBooks: List<DismissedSeriesBook> = emptyList(),
    val hiddenSeries: List<DismissedSeries> = emptyList(),
    // Keyed by DismissedSeriesBook.bookId / DismissedSeries.seriesId respectively — two maps
    // because book ids and series ids share the same Int namespace. Mapped by DismissedBooksCollector
    // / DismissedSeriesCollector directly (component-contract.md § 7.2 R9); no snapshot type and no
    // distinctUntilChanged here since each source flow's emission is already the dedupe point.
    val hiddenBookCovers: Map<Int, CoverUiModel> = emptyMap(),
    val hiddenSeriesCovers: Map<Int, CoverUiModel> = emptyMap(),
    val initialized: Boolean = false,
) : UiState {
    val isEmpty: Boolean
        get() = initialized && hiddenBooks.isEmpty() && hiddenSeries.isEmpty()
}
