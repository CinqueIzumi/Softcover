package nl.rhaydus.softcover.showcase.live.snackbar.event

import nl.rhaydus.toad.UiEvent

internal sealed interface SnackbarLiveEvent : UiEvent {
    data object NavigateBack : SnackbarLiveEvent
}
