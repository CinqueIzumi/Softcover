package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.softcover.feature.widget.presentation.event.TrendingSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.TrendingSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsUiState
import nl.rhaydus.toad.UiAction

internal sealed interface TrendingSettingsAction : UiAction<
    TrendingSettingsDependencies,
    TrendingSettingsUiState,
    TrendingSettingsEvent,
    TrendingSettingsLocalVariables,
    >
