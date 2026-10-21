package nl.rhaydus.softcover.showcase.live.field.event

import nl.rhaydus.toad.UiEvent

internal sealed interface FieldLiveEvent : UiEvent {
    data object NavigateBack : FieldLiveEvent
}
