package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import timber.log.Timber

data class OnNewEditionSaveClickAction(val edition: BookEdition) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val userBook = scope.currentState.book?.userBook ?: return

        dependencies.launch {
            scope.setState {
                it.copy(loadingBookDetails = true)
            }

            dependencies.updateBookEditionUseCase(
                userBook = userBook,
                newEditionId = edition.id
            ).onFailure {
                Timber.e("Something went wrong updating book edition! $it")
            }

            scope.setState {
                it.copy(loadingBookDetails = false)
            }
        }

        scope.setState {
            it.copy(showEditEditionSheet = false)
        }
    }
}