package nl.rhaydus.softcover.feature.explore.presentation.event

import nl.rhaydus.toad.UiEvent

internal sealed interface ExploreEvent : UiEvent {
    class BookAddedEvent : ExploreEvent

    class AddBookFailedEvent : ExploreEvent
}
