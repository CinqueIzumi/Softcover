package nl.rhaydus.softcover.feature.explore.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
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
    val emptyState: EmptyStateUiModel = EmptyStateUiModel(
        title = "Nothing hidden",
        body = "Books and series you mute from “Up next in your series” land here, " +
            "so you can bring any of them back whenever you like.",
    ),
    // `null` until the surface has loaded.
    val hiddenBookCards: ImmutableList<BookCardUiModel>? = null,
    val hiddenSeriesCards: ImmutableList<BookCardUiModel>? = null,
    val hiddenBooksLoaded: Boolean = false,
    val hiddenSeriesLoaded: Boolean = false,
) : UiState {
    val isEmpty: Boolean
        get() = hiddenBooksLoaded && hiddenSeriesLoaded && hiddenBooks.isEmpty() && hiddenSeries.isEmpty()
}
