package nl.rhaydus.softcover.feature.library.presentation.action

import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.withSelectedBookIds
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
            scope.setState { it.copy(isBulkRemoveSheetRequested = false) }

            return
        }

        scope.setState {
            it.copy(bulkActionInProgress = true)
        }

        dependencies.undoableBookRemover.remove(books = books)

        scope.setState {
            it.withSelectedBookIds(persistentSetOf()).copy(
                selectionMode = false,
                bulkActionInProgress = false,
                isBulkMoveMenuExpanded = false,
                isBulkRemoveSheetRequested = false,
                isChooseListsSheetRequested = false,
                listsBeingMutated = persistentSetOf(),
            )
        }
    }
}
