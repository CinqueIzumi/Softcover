package nl.rhaydus.softcover.showcase.live.chrome.event

import nl.rhaydus.toad.UiEvent

internal sealed interface ChromeLiveEvent : UiEvent {
    data object NavigateBack : ChromeLiveEvent
}
