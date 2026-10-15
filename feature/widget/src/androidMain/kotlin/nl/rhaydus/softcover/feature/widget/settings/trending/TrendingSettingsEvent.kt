package nl.rhaydus.softcover.feature.widget.settings.trending

import nl.rhaydus.toad.UiEvent

internal sealed interface TrendingSettingsEvent : UiEvent {
    data object Finished : TrendingSettingsEvent
}
