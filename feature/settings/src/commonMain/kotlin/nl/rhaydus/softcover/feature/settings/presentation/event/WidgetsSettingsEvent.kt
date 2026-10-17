package nl.rhaydus.softcover.feature.settings.presentation.event

import nl.rhaydus.toad.UiEvent

internal sealed interface WidgetsSettingsEvent : UiEvent {
    data object NavigateBack : WidgetsSettingsEvent
}
