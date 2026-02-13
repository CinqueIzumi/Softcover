package nl.rhaydus.softcover.feature.book.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book.presentation.screenmodel.BookDetailDependencies

data class OnUpdatePageProgressClickAction(
    val newPage: String,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val bookToUpdate: Book = scope.currentState.book ?: return

        val newPageValue = newPage.toIntOrNull() ?: 0

        dependencies.launch {
            dependencies.updateBookProgress(
                book = bookToUpdate,
                newPage = newPageValue,
                setLoading = { newValue ->
                    scope.setState {
                        it.copy(loading = newValue)
                    }
                }
            )
        }

        scope.setState {
            it.copy(showUpdateProgressSheet = false)
        }
    }
}