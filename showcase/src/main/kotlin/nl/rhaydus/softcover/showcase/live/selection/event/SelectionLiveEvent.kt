package nl.rhaydus.softcover.showcase.live.selection.event

import nl.rhaydus.toad.UiEvent

internal sealed interface SelectionLiveEvent : UiEvent {
    data object NavigateBack : SelectionLiveEvent
}
