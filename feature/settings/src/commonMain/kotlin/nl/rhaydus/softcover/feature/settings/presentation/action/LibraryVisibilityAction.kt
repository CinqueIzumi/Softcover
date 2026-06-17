package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.toad.UiAction

internal sealed interface LibraryVisibilityAction : UiAction<
    LibraryVisibilitySettingsDependencies,
    LibraryVisibilitySettingsUiState,
    LibraryVisibilitySettingsEvent,
    LibraryVisibilitySettingsLocalVariables,
    >
