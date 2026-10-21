package nl.rhaydus.softcover.showcase.live.verdict.event

import nl.rhaydus.toad.UiEvent

internal sealed interface VerdictLiveEvent : UiEvent {
    data object NavigateBack : VerdictLiveEvent
}
