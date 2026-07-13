package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
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
            scope.setState { it.copy(isBulkRemoveDialogShown = false) }

            return
        }

        scope.setState {
            it.copy(
                isBulkRemoveDialogShown = false,
                bulkActionInProgress = true,
            )
        }

        var failureCount = 0

        for (book in books) {
            dependencies.removeBookFromLibraryUseCase(book = book).onFailure { throwable ->
                AppLog.e(
                    throwable,
                    "Bulk remove failed for book ${book.id}",
                )

                failureCount++
            }
        }

        if (failureCount > 0) {
            val noun = if (failureCount == 1) "book" else "books"

            SnackBarManager.showSnackbar(title = "Couldn't remove $failureCount $noun — try again.")
        }

        scope.setState {
            it.copy(
                selectionMode = false,
                selectedBookIds = emptySet(),
                bulkActionInProgress = false,
                isBulkMoveMenuExpanded = false,
                isBulkRemoveDialogShown = false,
                isBulkAddToListSheetShown = false,
                listsBeingMutated = emptySet(),
            )
        }
    }
}
