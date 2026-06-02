package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import timber.log.Timber

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
            ).onFailure {
                Timber.e("Something went wrong updating book edition! $it")
            }

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
