package nl.rhaydus.softcover.showcase.live.control.event

import nl.rhaydus.toad.UiEvent

internal sealed interface ControlLiveEvent : UiEvent {
    data object NavigateBack : ControlLiveEvent
}
