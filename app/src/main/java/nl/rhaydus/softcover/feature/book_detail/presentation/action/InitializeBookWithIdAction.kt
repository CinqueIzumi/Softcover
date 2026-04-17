package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import timber.log.Timber

class InitializeBookWithIdAction(
    val id: Int,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        dependencies.launch {
            val result = dependencies
                .fetchBookByIdUseCase(id = id)
                .onFailure { Timber.e("-=- $it") }
                .getOrNull()

            scope.setState {
                it.copy(
                    book = result,
                    editions = result?.editions ?: emptyList(),
                    loadingBookDetails = false,
                )
            }

            result?.let { book ->
                scope.setLocalVariables { locals ->
                    locals.copy(editionsLoadedForBookId = book.id)
                }
            }
        }
    }
}
