package nl.rhaydus.softcover.feature.widget.presentation.collector

import nl.rhaydus.softcover.feature.widget.presentation.event.TrendingSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.TrendingSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsUiState
import nl.rhaydus.toad.Collector

internal sealed interface TrendingSettingsCollector : Collector<
    TrendingSettingsUiState,
    TrendingSettingsEvent,
    TrendingSettingsDependencies,
    TrendingSettingsLocalVariables,
    >
