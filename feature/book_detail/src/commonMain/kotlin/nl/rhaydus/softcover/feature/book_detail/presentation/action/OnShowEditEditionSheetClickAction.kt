package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.logging.AppLog
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

        scope.setState { it.copy(showEditEditionSheet = true) }

        if (alreadyLoaded) return

        scope.setState { it.copy(loadingEditions = true) }

        dependencies.launch {
            dependencies.getEditionsByBookIdUseCase(bookId = bookId)
                .onSuccess { editions ->
                    scope.setLocalVariables { it.copy(editionsLoadedForBookId = bookId) }
                    scope.setState {
                        it.copy(
                            editions = editions,
                            loadingEditions = false,
                        )
                    }
                }
                .onFailure { throwable ->
                    AppLog.e("Failed to fetch editions for book $bookId: $throwable")

                    scope.setState { it.copy(loadingEditions = false) }
                }
        }
    }
}
