package nl.rhaydus.softcover.feature.settings.presentation.collector

import nl.rhaydus.softcover.feature.settings.presentation.event.WidgetsSettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.WidgetsSettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.WidgetsSettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.WidgetsSettingsUiState
import nl.rhaydus.toad.Collector

internal sealed interface WidgetsSettingsCollector : Collector<
    WidgetsSettingsUiState,
    WidgetsSettingsEvent,
    WidgetsSettingsDependencies,
    WidgetsSettingsLocalVariables,
    >
