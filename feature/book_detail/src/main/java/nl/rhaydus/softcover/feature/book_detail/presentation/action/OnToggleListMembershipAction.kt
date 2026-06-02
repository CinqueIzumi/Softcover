package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import timber.log.Timber

class OnToggleListMembershipAction(
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

        scope.setState { it.copy(listsBeingMutated = it.listsBeingMutated + listId) }

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

        result.onFailure { error ->
            Timber.e(error, "Failed to toggle list membership for list $listId")
        }

        scope.setState { it.copy(listsBeingMutated = it.listsBeingMutated - listId) }
    }
}
