package nl.rhaydus.softcover.feature.settings.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

class UserListsCollector : LibraryVisibilityInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>,
        dependencies: LibraryVisibilitySettingsDependencies,
    ) {
        dependencies.getAllUserListsUseCase().collectLatest { lists ->
            scope.setState { it.copy(availableLists = lists.sortedBy { list -> list.name.lowercase() }) }
        }
    }
}
