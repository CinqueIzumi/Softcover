package nl.rhaydus.softcover.feature.reading.presentation.event

import nl.rhaydus.toad.UiEvent

internal sealed interface ReadingScreenEvent : UiEvent {
    class MarkAsReadFailedEvent : ReadingScreenEvent

    class ProgressUpdatedEvent : ReadingScreenEvent

    class ProgressUpdateFailedEvent : ReadingScreenEvent
}
