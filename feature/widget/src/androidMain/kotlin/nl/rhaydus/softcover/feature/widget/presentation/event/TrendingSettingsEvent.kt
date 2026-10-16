package nl.rhaydus.softcover.feature.widget.presentation.event

import nl.rhaydus.toad.UiEvent

internal sealed interface TrendingSettingsEvent : UiEvent {
    data object Finished : TrendingSettingsEvent
}
