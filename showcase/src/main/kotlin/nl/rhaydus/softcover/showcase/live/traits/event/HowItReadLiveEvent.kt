package nl.rhaydus.softcover.showcase.live.traits.event

import nl.rhaydus.toad.UiEvent

internal sealed interface HowItReadLiveEvent : UiEvent {
    data object NavigateBack : HowItReadLiveEvent
}
