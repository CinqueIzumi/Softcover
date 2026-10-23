package nl.rhaydus.softcover.showcase.live.dialog.event

import nl.rhaydus.toad.UiEvent

internal sealed interface DialogLiveEvent : UiEvent {
    data object NavigateBack : DialogLiveEvent
}
