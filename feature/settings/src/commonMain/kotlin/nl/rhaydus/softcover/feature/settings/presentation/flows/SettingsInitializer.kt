package nl.rhaydus.softcover.feature.settings.presentation.flows

import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.Collector

internal sealed interface SettingsInitializer : Collector<
        SettingsScreenUiState,
        SettingsScreenEvent,
        SettingsScreenDependencies,
        SettingsLocalVariables,
        >
