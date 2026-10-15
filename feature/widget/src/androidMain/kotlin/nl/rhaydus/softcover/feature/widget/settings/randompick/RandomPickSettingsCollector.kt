package nl.rhaydus.softcover.feature.widget.settings.randompick

import nl.rhaydus.toad.Collector

internal sealed interface RandomPickSettingsCollector : Collector<
    RandomPickSettingsUiState,
    RandomPickSettingsEvent,
    RandomPickSettingsDependencies,
    RandomPickSettingsLocalVariables,
    >
