package nl.rhaydus.softcover.showcase.live.progress.event

import nl.rhaydus.toad.UiEvent

internal sealed interface ProgressLiveEvent : UiEvent {
    data object NavigateBack : ProgressLiveEvent
}
