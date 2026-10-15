package nl.rhaydus.softcover.feature.widget.settings.action

import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsDependencies
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsUiState
import nl.rhaydus.toad.UiAction

internal sealed interface RandomPickSettingsAction : UiAction<
    RandomPickSettingsDependencies,
    RandomPickSettingsUiState,
    RandomPickSettingsEvent,
    RandomPickSettingsLocalVariables,
    >
