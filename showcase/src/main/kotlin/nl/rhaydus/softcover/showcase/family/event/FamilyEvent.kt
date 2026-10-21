package nl.rhaydus.softcover.showcase.family.event

import nl.rhaydus.toad.UiEvent

internal sealed interface FamilyEvent : UiEvent {
    data object NavigateBack : FamilyEvent
}
