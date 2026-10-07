package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.discardSheetScaffold
import nl.rhaydus.toad.ActionScope

internal class OnDismissDiscardSheetAction : LibraryVisibilityAction {
    override suspend fun execute(
        dependencies: LibraryVisibilitySettingsDependencies,
        scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>,
    ) {
        val shouldLeave = scope.currentState.leaveOnSheetDismissed

        scope.setState {
            it.copy(
                showDiscardSheet = false,
                discardSheetScaffold = discardSheetScaffold(),
                leaveOnSheetDismissed = false,
            )
        }

        if (shouldLeave) scope.sendEvent(event = LibraryVisibilitySettingsEvent.NavigateBack)
    }
}
