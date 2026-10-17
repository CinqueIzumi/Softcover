package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.presentation.error.toUserMessage
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnShowEditEditionSheetClickAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val bookId = scope.currentState.book?.id ?: return
        val alreadyLoaded = scope.currentLocalVariables.editionsLoadedForBookId == bookId

        scope.setState {
            it.copy(
                editionSheetRequested = true,
                selectedEditionId = it.displayedEdition?.id,
            )
        }

        if (alreadyLoaded) return

        scope.setState {
            it.copy(
                loadingEditions = true,
                editionsError = null,
            )
        }

        dependencies.launch {
            dependencies.getEditionsByBookIdUseCase(bookId = bookId)
                .onSuccess { editions ->
                    scope.setLocalVariables { it.copy(editionsLoadedForBookId = bookId) }
                    scope.setState {
                        it.copy(
                            editions = editions.toImmutableList(),
                            loadingEditions = false,
                        )
                    }
                }
                .onFailure { throwable ->
                    AppLog.e("Failed to fetch editions for book $bookId: $throwable")

                    scope.setState {
                        it.copy(
                            loadingEditions = false,
                            editionsError = throwable.toUserMessage() ?: LOAD_FAILURE_MESSAGE,
                        )
                    }
                }
        }
    }

    private companion object {
        const val LOAD_FAILURE_MESSAGE = "Couldn't load the editions."
    }
}
