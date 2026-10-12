package nl.rhaydus.softcover.feature.library.presentation.action

import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.component.lists.ListMembership
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Add or remove the current selection to/from a custom list. Membership semantics mirror the
 * tristate pattern in `ChooseListsBottomSheet`:
 *
 * - [ListMembership.ALL] → remove every selected book from the list.
 * - [ListMembership.NONE] or [ListMembership.PARTIAL] → add the books that are not already on
 *   the list (committing toward [ListMembership.ALL]).
 *
 * The action does **not** exit selection mode — the user may want to add the same selection to
 * several lists in sequence.
 */
internal class OnBulkToggleListMembershipAction(
    private val listId: Int,
    private val currentMembership: ListMembership,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        val state = scope.currentState

        if (listId in state.listsBeingMutated) return

        val list = state.customLists.firstOrNull { it.id == listId } ?: return
        val books = state.resolveSelectedBooks()

        if (books.isEmpty()) return

        val existingBookIdsOnList = list.books.mapTo(mutableSetOf()) { it.bookId }

        scope.setState { it.copy(listsBeingMutated = it.listsBeingMutated.toPersistentSet().add(listId)) }

        var failureCount = 0
        val isRemove = currentMembership == ListMembership.ALL

        try {
            when (currentMembership) {
                ListMembership.ALL -> {
                    for (book in books) {
                        if ((book.id in existingBookIdsOnList).not()) continue

                        dependencies.removeBookFromListUseCase(
                            listId = listId,
                            bookId = book.id,
                        ).onFailure { throwable ->
                            AppLog.e(
                                throwable,
                                "Bulk remove from list $listId failed for book ${book.id}",
                            )

                            failureCount++
                        }
                    }
                }

                ListMembership.NONE,
                ListMembership.PARTIAL,
                    -> {
                    for (book in books) {
                        if (book.id in existingBookIdsOnList) continue

                        val edition = book.currentEdition ?: book.defaultEdition

                        if (edition == null) {
                            AppLog.e("Bulk add to list $listId skipped book ${book.id}: no edition available")

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
                                "Bulk add to list $listId failed for book ${book.id}",
                            )

                            failureCount++
                        }
                    }
                }
            }

            val attemptedWriteCount = books.count { (it.id in existingBookIdsOnList) == isRemove }

            if (failureCount < attemptedWriteCount) {
                withTimeoutOrNull(DATA_WAIT_MILLIS) {
                    scope.state.first { current ->
                        val bookIds = current.customLists
                            .firstOrNull { it.id == listId }
                            ?.books
                            ?.mapTo(mutableSetOf()) { it.bookId }

                        bookIds == null || bookIds != existingBookIdsOnList
                    }
                }
            }
        } finally {
            withContext(NonCancellable) {
                scope.setState { it.copy(listsBeingMutated = it.listsBeingMutated.toPersistentSet().remove(listId)) }
            }
        }

        if (failureCount > 0) {
            val noun = if (failureCount == 1) "book" else "books"
            val verb = if (isRemove) "remove" else "add"
            val preposition = if (isRemove) "from" else "to"

            SnackBarManager.showSnackbar(
                title = "Couldn't $verb $failureCount $noun $preposition \"${list.name}\". Try again.",
            )

            scope.sendEvent(LibraryEvent.ListMembershipChangeFailedEvent())
        } else {
            val feedback = if (isRemove) null else Feedback.Routine

            scope.sendEvent(LibraryEvent.ListMembershipChangedEvent(feedback = feedback))
        }
    }
}

private const val DATA_WAIT_MILLIS = 2_000L
