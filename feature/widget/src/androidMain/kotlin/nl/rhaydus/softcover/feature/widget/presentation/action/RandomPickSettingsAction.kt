package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.softcover.feature.widget.presentation.event.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.RandomPickSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.RandomPickSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.RandomPickSettingsUiState
import nl.rhaydus.toad.UiAction

internal sealed interface RandomPickSettingsAction : UiAction<
    RandomPickSettingsDependencies,
    RandomPickSettingsUiState,
    RandomPickSettingsEvent,
    RandomPickSettingsLocalVariables,
    >
