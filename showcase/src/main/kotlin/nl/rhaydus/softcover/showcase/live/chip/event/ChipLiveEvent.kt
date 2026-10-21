package nl.rhaydus.softcover.showcase.live.chip.event

import nl.rhaydus.toad.UiEvent

internal sealed interface ChipLiveEvent : UiEvent {
    data object NavigateBack : ChipLiveEvent
}
