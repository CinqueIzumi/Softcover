package nl.rhaydus.softcover.feature.session.presentation.event

import nl.rhaydus.toad.UiEvent

internal sealed interface FocusModeEvent : UiEvent {
    class PageCommittedEvent : FocusModeEvent
}
