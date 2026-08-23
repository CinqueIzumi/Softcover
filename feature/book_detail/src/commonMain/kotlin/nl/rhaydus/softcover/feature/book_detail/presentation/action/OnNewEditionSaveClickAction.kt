package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.error.onApiFailure
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal data class OnNewEditionSaveClickAction(val edition: BookEdition) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val userBook = scope.currentState.book?.userBook

        // No user book yet: switching editions is a local-only preview. Show the chosen edition's
        // details without creating or mutating a user book and without caching anything.
        if (userBook == null) {
            scope.setState {
                it.copy(
                    previewEdition = edition,
                    scannedEditionId = null,
                    showEditEditionSheet = false,
                )
            }

            return
        }

        dependencies.launch {
            scope.setState {
                it.copy(loadingBookDetails = true)
            }

            dependencies.updateBookEditionUseCase(
                userBook = userBook,
                newEditionId = edition.id,
            ).onApiFailure()

            scope.setState {
                it.copy(loadingBookDetails = false)
            }
        }

        scope.setState {
            it.copy(
                scannedEditionId = null,
                showEditEditionSheet = false,
            )
        }
    }
}
