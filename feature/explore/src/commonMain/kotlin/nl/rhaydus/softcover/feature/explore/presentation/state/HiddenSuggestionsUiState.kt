package nl.rhaydus.softcover.feature.explore.presentation.state

import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.feature.explore.presentation.action.HiddenSuggestionsAction
import nl.rhaydus.softcover.feature.explore.presentation.mapper.HIDDEN_BOOK_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.mapper.HIDDEN_SERIES_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.model.HiddenGroupHeaderUiModel
import nl.rhaydus.toad.UiState

private const val HIDDEN_SUGGESTIONS_TITLE = "Hidden suggestions"

internal data class HiddenSuggestionsUiState(
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
    // Loading stand-ins until the surface has loaded; each card's payload is the action that restores it.
    val hiddenBooks: BookCardSet<HiddenSuggestionsAction> = BookCardSet.standIns(HIDDEN_BOOK_STAND_IN_CARDS),
    val hiddenSeries: BookCardSet<HiddenSuggestionsAction> = BookCardSet.standIns(HIDDEN_SERIES_STAND_IN_CARDS),
    val topBar: TopBarUiModel = TopBarUiModel(
        title = HIDDEN_SUGGESTIONS_TITLE,
    ),
) : UiState {
    val isLoadingBooks: Boolean
        get() = hiddenBooks.cards.any(BookCardUiModel::isLoading)

    val isLoadingSeries: Boolean
        get() = hiddenSeries.cards.any(BookCardUiModel::isLoading)

    val isEmpty: Boolean
        get() = isLoadingBooks.not() && isLoadingSeries.not() && hiddenBooks.isEmpty && hiddenSeries.isEmpty
}
