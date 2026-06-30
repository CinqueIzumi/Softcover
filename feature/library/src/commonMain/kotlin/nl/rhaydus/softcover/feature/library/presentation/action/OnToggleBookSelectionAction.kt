package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class OnToggleBookSelectionAction(
    private val bookId: Int,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState {
            val next = if (bookId in it.selectedBookIds) {
                it.selectedBookIds - bookId
            } else {
                it.selectedBookIds + bookId
            }

            // Deselecting the last book collapses the mode rather than leaving the user
            // staring at an action bar with zero targets.
            if (next.isEmpty()) {
                it.copy(
                    selectionMode = false,
                    selectedBookIds = emptySet(),
                    isBulkMoveMenuExpanded = false,
                    isBulkRemoveDialogShown = false,
                    isBulkAddToListSheetShown = false,
                    listsBeingMutated = emptySet(),
                )
            } else {
                it.copy(selectedBookIds = next)
            }
        }
    }
}
