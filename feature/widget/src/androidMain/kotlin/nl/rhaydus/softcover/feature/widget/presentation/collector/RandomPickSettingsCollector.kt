package nl.rhaydus.softcover.feature.widget.presentation.collector

import nl.rhaydus.softcover.feature.widget.presentation.event.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.RandomPickSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.RandomPickSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.RandomPickSettingsUiState
import nl.rhaydus.toad.Collector

internal sealed interface RandomPickSettingsCollector : Collector<
    RandomPickSettingsUiState,
    RandomPickSettingsEvent,
    RandomPickSettingsDependencies,
    RandomPickSettingsLocalVariables,
    >
