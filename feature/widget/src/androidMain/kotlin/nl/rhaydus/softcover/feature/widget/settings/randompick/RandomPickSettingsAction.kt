package nl.rhaydus.softcover.feature.widget.settings.randompick

import nl.rhaydus.toad.UiAction

internal sealed interface RandomPickSettingsAction : UiAction<
    RandomPickSettingsDependencies,
    RandomPickSettingsUiState,
    RandomPickSettingsEvent,
    RandomPickSettingsLocalVariables,
    >
