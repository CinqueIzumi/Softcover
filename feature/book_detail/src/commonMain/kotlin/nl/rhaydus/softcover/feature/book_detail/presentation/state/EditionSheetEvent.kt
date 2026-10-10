package nl.rhaydus.softcover.feature.book_detail.presentation.state

internal sealed interface EditionSheetEvent {
    data class SearchChanged(val query: String) : EditionSheetEvent

    data class EditionSelected(val editionId: Int) : EditionSheetEvent

    data object Confirmed : EditionSheetEvent

    data object Dismissed : EditionSheetEvent
}
