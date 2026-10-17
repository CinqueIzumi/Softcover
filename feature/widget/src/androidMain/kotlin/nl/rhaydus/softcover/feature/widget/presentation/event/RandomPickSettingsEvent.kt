package nl.rhaydus.softcover.feature.widget.presentation.event

import nl.rhaydus.toad.UiEvent

internal sealed interface RandomPickSettingsEvent : UiEvent {
    data object Finished : RandomPickSettingsEvent
}
