package nl.rhaydus.softcover.showcase.live.quote.event

import nl.rhaydus.toad.UiEvent

internal sealed interface QuoteLiveEvent : UiEvent {
    data object NavigateBack : QuoteLiveEvent
}
