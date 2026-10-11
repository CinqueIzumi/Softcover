package nl.rhaydus.softcover.feature.settings.presentation.action

import kotlinx.collections.immutable.toPersistentSet
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.withOrderedEntries
import nl.rhaydus.toad.ActionScope

internal class OnStatusToggleAction(
    private val code: Int,
    private val enabled: Boolean,
) : LibraryVisibilityAction {
    override suspend fun execute(
        dependencies: LibraryVisibilitySettingsDependencies,
        scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>,
    ) {
        scope.setState { state ->
            val updated = state.draftEnabledStatusCodes.toPersistentSet().let { current ->
                if (enabled) current.add(code) else current.remove(code)
            }
            state.copy(draftEnabledStatusCodes = updated).withOrderedEntries()
        }
    }
}
