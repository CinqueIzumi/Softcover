package nl.rhaydus.softcover.feature.widget.settings.trending

import nl.rhaydus.toad.Collector

internal sealed interface TrendingSettingsCollector : Collector<
    TrendingSettingsUiState,
    TrendingSettingsEvent,
    TrendingSettingsDependencies,
    TrendingSettingsLocalVariables,
    >
