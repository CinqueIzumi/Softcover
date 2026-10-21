package nl.rhaydus.softcover.showcase.live.callout.event

import nl.rhaydus.toad.UiEvent

internal sealed interface CalloutLiveEvent : UiEvent {
    data object NavigateBack : CalloutLiveEvent
}
