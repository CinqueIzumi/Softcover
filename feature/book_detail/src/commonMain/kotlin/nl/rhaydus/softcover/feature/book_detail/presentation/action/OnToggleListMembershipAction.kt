package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnToggleListMembershipAction(
    private val listId: Int,
    private val isMember: Boolean,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val book = scope.currentState.book ?: return

        val edition = book.currentEdition ?: book.defaultEdition ?: return

        if (listId in scope.currentState.listsBeingMutated) return

        val bookIdsBeforeWrite = scope.currentState.userLists.bookIdsOf(listId)

        scope.setState { it.copy(listsBeingMutated = it.listsBeingMutated.toPersistentSet().add(listId)) }

        try {
            val result = if (isMember) {
                dependencies.removeBookFromListUseCase(
                    listId = listId,
                    bookId = book.id,
                )
            } else {
                dependencies.addBookToListUseCase(
                    listId = listId,
                    bookId = book.id,
                    edition = edition,
                )
            }

            result
                .onSuccess {
                    val feedback = if (isMember) null else Feedback.Routine

                    scope.sendEvent(BookDetailEvent.MutationSucceededEvent(feedback = feedback))
                }
                .onFailure { error ->
                    AppLog.e(
                        error,
                        "Failed to toggle list membership for list $listId",
                    )

                    scope.sendEvent(BookDetailEvent.MutationFailedEvent())
                }

            if (result.isSuccess) {
                withTimeoutOrNull(DATA_WAIT_MILLIS) {
                    scope.state.first { state ->
                        val bookIds = state.userLists.bookIdsOf(listId)

                        bookIds == null || bookIds != bookIdsBeforeWrite
                    }
                }
            }
        } finally {
            withContext(NonCancellable) {
                scope.setState { it.copy(listsBeingMutated = it.listsBeingMutated.toPersistentSet().remove(listId)) }
            }
        }
    }

    private fun List<BookList>.bookIdsOf(listId: Int): Set<Int>? = firstOrNull { it.id == listId }
        ?.books
        ?.mapTo(mutableSetOf()) { it.bookId }
}

private const val DATA_WAIT_MILLIS = 2_000L
