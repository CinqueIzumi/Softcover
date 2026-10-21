package nl.rhaydus.softcover.showcase.live.share.event

import nl.rhaydus.toad.UiEvent

internal sealed interface ShareLiveEvent : UiEvent {
    data object NavigateBack : ShareLiveEvent
}
