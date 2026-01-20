package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.UiAction
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.softcover.feature.settings.presentation.viewmodel.SettingsDependencies

sealed interface SettingsAction : UiAction<
        SettingsDependencies,
        SettingsScreenUiState,
        SettingsEvent,
        >