package nl.rhaydus.softcover.feature.explore.presentation.model

internal sealed interface BecauseYouReadGenreSheetEvent {
    data object Dismissed : BecauseYouReadGenreSheetEvent

    data class GenreSelected(val genre: String?) : BecauseYouReadGenreSheetEvent
}
