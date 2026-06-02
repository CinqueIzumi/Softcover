package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

class OnReorderLibraryTabsAction(
    private val newOrderedIds: List<String>,
) : LibraryVisibilityAction {
    override suspend fun execute(
        dependencies: LibraryVisibilitySettingsDependencies,
        scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>,
    ) {
        scope.setState { state ->
            state.copy(draftTabOrder = newOrderedIds)
        }
    }
}
