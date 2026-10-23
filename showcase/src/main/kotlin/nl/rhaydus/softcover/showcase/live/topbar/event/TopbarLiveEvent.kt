package nl.rhaydus.softcover.showcase.live.topbar.event

import nl.rhaydus.toad.UiEvent

internal sealed interface TopbarLiveEvent : UiEvent {
    data object NavigateBack : TopbarLiveEvent
}
