package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

class OnTabSelectedAction(
    private val tabId: String,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        // Switching tabs collapses selection mode — selection is anchored to the tab the user
        // long-pressed on, and silently carrying it across tabs (each of which has its own visible
        // set) makes the count subtitle lie.
        scope.setState {
            it.copy(
                selectedTabId = tabId,
                selectionMode = false,
                selectedBookIds = emptySet(),
                isBulkMoveMenuExpanded = false,
                isBulkRemoveDialogShown = false,
                isBulkAddToListSheetShown = false,
                listsBeingMutated = emptySet(),
            )
        }
    }
}
