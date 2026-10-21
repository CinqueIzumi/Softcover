package nl.rhaydus.softcover.showcase.live.celebration.event

import nl.rhaydus.toad.UiEvent

internal sealed interface CelebrationLiveEvent : UiEvent {
    data object NavigateBack : CelebrationLiveEvent
}
