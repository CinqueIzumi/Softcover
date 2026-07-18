package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Resumes bulk add-to-list after the reader broke off to create a new list: the selection lands on the
 * new list, and the chooser they started in reopens with it on screen and ticked.
 *
 * Distinct from [OnBulkToggleListMembershipAction] rather than a special case of it, because that
 * action resolves the list out of `customLists` and returns early when it is absent. A list created a
 * moment ago has been written to the database but has only *started* its trip back to screen state
 * through the lists `Flow`, so it is routinely still missing here — routing this through the toggle
 * action would silently drop the books often enough to look random. Nothing needs looking up anyway: a
 * brand-new list holds no books, so every selected book is an add, and [listName] is carried in for
 * messaging rather than read back out of state.
 */
internal class OnBulkAddToNewListAction(
    private val listId: Int,
    private val listName: String,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        val books = scope.currentState.resolveSelectedBooks()

        // Reopened first so the chooser is already back by the time the writes land — the rows show
        // their own in-flight state, which is the feedback this path wants.
        scope.setState { it.copy(isBulkAddToListSheetShown = true) }

        if (books.isEmpty()) return

        scope.setState { it.copy(listsBeingMutated = it.listsBeingMutated + listId) }

        var failureCount = 0

        for (book in books) {
            val edition = book.currentEdition ?: book.defaultEdition

            if (edition == null) {
                AppLog.e("Add to new list $listId skipped book ${book.id}: no edition available")

                failureCount++

                continue
            }

            dependencies.addBookToListUseCase(
                listId = listId,
                bookId = book.id,
                edition = edition,
            ).onFailure { throwable ->
                AppLog.e(
                    throwable,
                    "Add to new list $listId failed for book ${book.id}",
                )

                failureCount++
            }
        }

        scope.setState { it.copy(listsBeingMutated = it.listsBeingMutated - listId) }

        if (failureCount > 0) {
            val noun = if (failureCount == 1) "book" else "books"

            SnackBarManager.showSnackbar(
                title = "Couldn't add $failureCount $noun to “$listName” — try again.",
            )
        }
    }
}
