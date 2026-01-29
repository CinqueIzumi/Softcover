package nl.rhaydus.softcover.feature.settings.presentation.flows

import nl.rhaydus.softcover.core.presentation.toad.FlowCollector
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.softcover.feature.settings.presentation.viewmodel.SettingsScreenDependencies

sealed interface SettingsFlowCollector : FlowCollector<
        SettingsScreenUiState,
        SettingsScreenEvent,
        SettingsScreenDependencies,
        SettingsLocalVariables,
        >