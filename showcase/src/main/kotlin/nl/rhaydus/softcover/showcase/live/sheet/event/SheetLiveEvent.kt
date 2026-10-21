package nl.rhaydus.softcover.showcase.live.sheet.event

import nl.rhaydus.toad.UiEvent

internal sealed interface SheetLiveEvent : UiEvent {
    data object NavigateBack : SheetLiveEvent
}
