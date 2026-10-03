package nl.rhaydus.softcover.feature.explore.presentation.state

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.presentation.model.HiddenGroupHeaderUiModel
import nl.rhaydus.toad.UiState

internal data class HiddenSuggestionsUiState(
    val hiddenBooks: List<DismissedSeriesBook> = emptyList(),
    val hiddenSeries: List<DismissedSeries> = emptyList(),
    val hiddenBooksGroupHeader: HiddenGroupHeaderUiModel = HiddenGroupHeaderUiModel(
        header = SectionHeaderUiModel.Inline(eyebrow = "Hidden books"),
        count = "0",
        subline = "Set aside — tap Restore to shelve them again.",
    ),
    val hiddenSeriesGroupHeader: HiddenGroupHeaderUiModel = HiddenGroupHeaderUiModel(
        header = SectionHeaderUiModel.Inline(eyebrow = "Hidden series"),
        count = "0",
        subline = "A muted series hides every book in it.",
    ),
    // Keyed by DismissedSeriesBook.bookId / DismissedSeries.seriesId respectively — two maps
    // because book ids and series ids share the same Int namespace.
    val hiddenBookCards: ImmutableMap<Int, BookCardUiModel> = persistentMapOf(),
    val hiddenSeriesCards: ImmutableMap<Int, BookCardUiModel> = persistentMapOf(),
    val initialized: Boolean = false,
) : UiState {
    val isEmpty: Boolean
        get() = initialized && hiddenBooks.isEmpty() && hiddenSeries.isEmpty()
}
