package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope

data class OnUpdatePageProgressClickAction(
    val newPage: String,
) : nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction {
    override suspend fun execute(
        dependencies: nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies,
        scope: ActionScope<nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState, nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent, nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables>,
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