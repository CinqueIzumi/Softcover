package nl.rhaydus.softcover.feature.widget.progress

import nl.rhaydus.toad.UiEvent

internal sealed interface UpdateProgressEvent : UiEvent {
    data object Finished : UpdateProgressEvent
}
