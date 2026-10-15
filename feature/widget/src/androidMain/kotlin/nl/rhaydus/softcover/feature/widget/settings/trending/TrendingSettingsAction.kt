package nl.rhaydus.softcover.feature.widget.settings.trending

import nl.rhaydus.toad.UiAction

internal sealed interface TrendingSettingsAction : UiAction<
    TrendingSettingsDependencies,
    TrendingSettingsUiState,
    TrendingSettingsEvent,
    TrendingSettingsLocalVariables,
    >
