package nl.rhaydus.softcover.feature.widget.settings.randompick

import nl.rhaydus.toad.UiEvent

internal sealed interface RandomPickSettingsEvent : UiEvent {
    data object Finished : RandomPickSettingsEvent
}
