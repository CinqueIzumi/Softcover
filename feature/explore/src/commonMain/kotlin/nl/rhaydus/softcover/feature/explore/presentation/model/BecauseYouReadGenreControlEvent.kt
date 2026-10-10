package nl.rhaydus.softcover.feature.explore.presentation.model

internal sealed interface BecauseYouReadGenreControlEvent {
    data object Opened : BecauseYouReadGenreControlEvent

    data object Dismissed : BecauseYouReadGenreControlEvent

    data class GenreSelected(val genre: String?) : BecauseYouReadGenreControlEvent
}
