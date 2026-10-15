package nl.rhaydus.softcover.feature.widget.settings.collector

import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsDependencies
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsUiState
import nl.rhaydus.toad.Collector

internal sealed interface RandomPickSettingsCollector : Collector<
    RandomPickSettingsUiState,
    RandomPickSettingsEvent,
    RandomPickSettingsDependencies,
    RandomPickSettingsLocalVariables,
    >
