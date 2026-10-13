package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.core.component.sheet.open
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.discardSheetScaffold
import nl.rhaydus.toad.ActionScope

internal class OnLibraryVisibilityBackClickAction : LibraryVisibilityAction {
    override suspend fun execute(
        dependencies: LibraryVisibilitySettingsDependencies,
        scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>,
    ) {
        if (scope.currentState.isDirty && scope.currentState.isSaving.not()) {
            scope.setState {
                it.copy(discardSheet = it.discardSheet.open(model = discardSheetScaffold()))
            }
        } else {
            scope.sendEvent(event = LibraryVisibilitySettingsEvent.NavigateBack)
        }
    }
}
