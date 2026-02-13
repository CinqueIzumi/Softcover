package nl.rhaydus.softcover.feature.book.presentation.action

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book.presentation.screenmodel.BookDetailDependencies
import timber.log.Timber

data class OnNewEditionSaveClickAction(val edition: BookEdition) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val book = scope.currentState.book ?: return
        val userBookId = book.userBook?.id ?: return

        dependencies.launch {
            scope.setState {
                it.copy(loading = true)
            }

            dependencies.updateBookEditionUseCase(
                userBookId = userBookId,
                newEditionId = edition.id
            ).onFailure {
                Timber.e("-=- Something went wrong updating book edition! $it")
            }

            scope.setState {
                it.copy(loading = false)
            }
        }

        scope.setState {
            it.copy(showEditEditionSheet = false)
        }
    }
}