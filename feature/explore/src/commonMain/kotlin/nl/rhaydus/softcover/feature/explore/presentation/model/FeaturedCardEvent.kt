package nl.rhaydus.softcover.feature.explore.presentation.model

internal sealed interface FeaturedCardEvent {
    data object Clicked : FeaturedCardEvent

    data object WantToReadClicked : FeaturedCardEvent

    data object RemoveFromLibraryClicked : FeaturedCardEvent
}
