package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class OnBulkRemoveFromLibraryAction(
    private val explicitBookIds: Set<Int>? = null,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        val state = scope.currentState
        val books = state.resolveSelectedBooks(explicitBookIds ?: state.selectedBookIds)

        if (books.isEmpty()) {
            scope.setState { it.copy(isBulkRemoveCloseRequested = true) }

            return
        }

        scope.setState {
            it.copy(bulkActionInProgress = true)
        }

        dependencies.undoableBookRemover.remove(books = books)

        scope.setState {
            it.copy(
                selectionMode = false,
                selectedBookIds = emptySet(),
                bulkActionInProgress = false,
                isBulkMoveMenuExpanded = false,
                isBulkRemoveCloseRequested = true,
                isBulkAddToListSheetShown = false,
                listsBeingMutated = emptySet(),
            )
        }
    }
}
