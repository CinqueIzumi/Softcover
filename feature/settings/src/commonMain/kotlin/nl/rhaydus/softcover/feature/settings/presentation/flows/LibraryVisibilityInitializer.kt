package nl.rhaydus.softcover.feature.settings.presentation.flows

import nl.rhaydus.softcover.core.designsystem.presentation.toad.Collector
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

internal sealed interface LibraryVisibilityInitializer : Collector<
    LibraryVisibilitySettingsUiState,
    LibraryVisibilitySettingsEvent,
    LibraryVisibilitySettingsDependencies,
    LibraryVisibilitySettingsLocalVariables,
    >
