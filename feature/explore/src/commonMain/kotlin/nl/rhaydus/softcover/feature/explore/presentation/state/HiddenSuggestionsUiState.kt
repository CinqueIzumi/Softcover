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
        header = SectionHeaderUiModel.Inline(
            eyebrow = "Hidden books",
            isLoading = true,
        ),
        count = "0",
        subline = "Hidden from Up next. Tap Restore to bring one back.",
    ),
    val hiddenSeriesGroupHeader: HiddenGroupHeaderUiModel = HiddenGroupHeaderUiModel(
        header = SectionHeaderUiModel.Inline(
            eyebrow = "Hidden series",
            isLoading = true,
        ),
        count = "0",
        subline = "A hidden series hides every book in it.",
    ),
    val hiddenRecommendationsGroupHeader: HiddenGroupHeaderUiModel = HiddenGroupHeaderUiModel(
        header = SectionHeaderUiModel.Inline(
            eyebrow = "Hidden recommendations",
            isLoading = true,
        ),
        count = "0",
        subline = "Hidden from Recommended for you and Top picks. Tap Restore to bring one back.",
    ),
    val emptyState: EmptyStateUiModel = EmptyStateUiModel(
        title = "Nothing hidden",
        body = "Books and series you hide from your suggestions end up here, in case you change your mind.",
    ),
    val intro: String = "Books and series you've hidden stay out of your suggestions on Explore and Library. " +
        "Restore one to see it again.",
    // Loading stand-ins until the surface has loaded; each card's payload is the action that restores it.
    val hiddenBooks: BookCardSet<HiddenSuggestionsAction> = BookCardSet.standIns(HIDDEN_BOOK_STAND_IN_CARDS),
    val hiddenSeries: BookCardSet<HiddenSuggestionsAction> = BookCardSet.standIns(HIDDEN_SERIES_STAND_IN_CARDS),
    val hiddenRecommendations: BookCardSet<HiddenSuggestionsAction> =
        BookCardSet.standIns(HIDDEN_BOOK_STAND_IN_CARDS),
    val topBar: TopBarUiModel = TopBarUiModel(
        title = HIDDEN_SUGGESTIONS_TITLE,
    ),
) : UiState {
    val isLoadingBooks: Boolean
        get() = hiddenBooks.cards.any(BookCardUiModel::isLoading)

    val isLoadingSeries: Boolean
        get() = hiddenSeries.cards.any(BookCardUiModel::isLoading)

    val isLoadingRecommendations: Boolean
        get() = hiddenRecommendations.cards.any(BookCardUiModel::isLoading)

    val isLoading: Boolean
        get() = isLoadingBooks || isLoadingSeries || isLoadingRecommendations

    val isEmpty: Boolean
        get() = isLoading.not() && hiddenBooks.isEmpty && hiddenSeries.isEmpty && hiddenRecommendations.isEmpty
}
