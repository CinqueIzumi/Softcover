package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import timber.log.Timber

data class OnNewEditionSaveClickAction(val edition: BookEdition) :
    nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction {
    override suspend fun execute(
        dependencies: nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies,
        scope: ActionScope<nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState, nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent, nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables>,
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