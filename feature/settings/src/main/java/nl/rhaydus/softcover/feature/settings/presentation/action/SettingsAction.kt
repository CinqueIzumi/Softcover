package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.UiAction
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

sealed interface SettingsAction : UiAction<
        SettingsScreenDependencies,
        SettingsScreenUiState,
        SettingsScreenEvent,
        SettingsLocalVariables
        >