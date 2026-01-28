package nl.rhaydus.softcover.feature.book.presentation.action

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book.presentation.viewmodel.BookDetailDependencies
import timber.log.Timber

data class OnNewEditionSaveClickAction(val edition: BookEdition) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent>,
    ) {
        val book = scope.currentState.book ?: return
        val userBookId = book.userBookId ?: return

        dependencies.launch {
            scope.setState {
                copy(loading = true)
            }

            dependencies.updateBookEditionUseCase(
                userBookId = userBookId,
                newEditionId = edition.id
            ).onFailure {
                Timber.e("-=- Something went wrong updating book edition! $it")
            }

            scope.setState {
                copy(loading = false)
            }
        }

        scope.setState {
            copy(showEditEditionSheet = false)
        }
    }
}