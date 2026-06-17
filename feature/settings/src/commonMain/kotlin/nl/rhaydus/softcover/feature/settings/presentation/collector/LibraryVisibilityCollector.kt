package nl.rhaydus.softcover.feature.settings.presentation.collector

import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.toad.Collector

internal sealed interface LibraryVisibilityCollector : Collector<
    LibraryVisibilitySettingsUiState,
    LibraryVisibilitySettingsEvent,
    LibraryVisibilitySettingsDependencies,
    LibraryVisibilitySettingsLocalVariables,
    >
