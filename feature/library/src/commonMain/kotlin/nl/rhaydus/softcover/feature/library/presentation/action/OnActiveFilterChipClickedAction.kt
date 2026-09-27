package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LIBRARY_CLEAR_ALL_CHIP_KEY
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class OnActiveFilterChipClickedAction(
    private val tabId: String,
    private val key: String,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        if (key == LIBRARY_CLEAR_ALL_CHIP_KEY) {
            OnClearFiltersAction(tabId = tabId).execute(
                dependencies = dependencies,
                scope = scope,
            )
            return
        }

        val value = scope.currentState.activeFilterValueByChipKey[key] ?: return

        OnToggleFilterValueAction(
            tabId = tabId,
            value = value,
        ).execute(
            dependencies = dependencies,
            scope = scope,
        )
    }
}
