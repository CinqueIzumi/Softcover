package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.designsystem.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import timber.log.Timber

internal class OnBulkRemoveFromLibraryAction : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        val state = scope.currentState
        val books = state.resolveSelectedBooks()

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
                Timber.e(
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
