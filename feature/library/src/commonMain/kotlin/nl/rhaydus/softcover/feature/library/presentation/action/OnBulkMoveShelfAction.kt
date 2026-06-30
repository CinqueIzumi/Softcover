package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class OnBulkMoveShelfAction(
    private val status: UserBookStatus,
    private val explicitBookIds: Set<Int>? = null,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        val state = scope.currentState
        val books = state.resolveSelectedBooks(explicitBookIds ?: state.selectedBookIds)

        if (books.isEmpty()) {
            scope.setState { it.copy(isBulkMoveMenuExpanded = false) }

            return
        }

        scope.setState {
            it.copy(
                isBulkMoveMenuExpanded = false,
                bulkActionInProgress = true,
            )
        }

        var failureCount = 0

        for (book in books) {
            val result = when (status) {
                UserBookStatus.READ -> dependencies.markBookAsReadUseCase(book = book)
                UserBookStatus.CURRENTLY_READING -> dependencies.markBookAsReadingUseCase(book = book)
                UserBookStatus.WANT_TO_READ -> dependencies.markBookAsWantToReadUseCase(book = book)
                UserBookStatus.DID_NOT_FINISH -> error(
                    "Bulk move-shelf does not support DID_NOT_FINISH",
                )
            }

            result.onFailure { throwable ->
                AppLog.e(
                    throwable,
                    "Bulk move-shelf failed for book ${book.id}",
                )

                failureCount++
            }
        }

        if (failureCount > 0) {
            val noun = if (failureCount == 1) "book" else "books"

            SnackBarManager.showSnackbar(title = "Couldn't move $failureCount $noun — try again.")
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
