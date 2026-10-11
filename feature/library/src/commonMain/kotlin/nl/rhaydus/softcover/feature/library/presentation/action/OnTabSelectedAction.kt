package nl.rhaydus.softcover.feature.library.presentation.action

import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class OnTabSelectedAction(
    private val tabId: String,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState {
            it.withTabSelected(tabId = tabId)
        }
    }
}

// Switching tabs collapses selection mode — selection is anchored to the tab the user
// long-pressed on, and silently carrying it across tabs (each of which has its own visible
// set) makes the count subtitle lie.
internal fun LibraryUiState.withTabSelected(tabId: String): LibraryUiState =
    copy(
        selectedTabId = tabId,
        selectionMode = false,
        selectedBookIds = persistentSetOf(),
        isBulkMoveMenuExpanded = false,
        isBulkRemoveSheetRequested = false,
        isChooseListsSheetRequested = false,
        listsBeingMutated = persistentSetOf(),
        isRearranging = false,
    )
