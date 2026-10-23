package nl.rhaydus.softcover.showcase.live.lists.event

import nl.rhaydus.toad.UiEvent

internal sealed interface ListsLiveEvent : UiEvent {
    data object NavigateBack : ListsLiveEvent
}
