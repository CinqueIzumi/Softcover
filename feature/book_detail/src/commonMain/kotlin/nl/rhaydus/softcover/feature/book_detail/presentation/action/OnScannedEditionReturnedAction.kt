package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Shows the edition the ISBN lookup resolved for this book. That edition may have just been created
 * on Hardcover, so the editions are refetched past the cache for it to be found.
 */
internal class OnScannedEditionReturnedAction(
    private val editionId: Int?,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val bookId = scope.currentState.book?.id ?: return

        scope.setLocalVariables { it.copy(editionsLoadedForBookId = null) }

        scope.setState {
            it.copy(
                scannedEditionId = editionId,
                scannedEditionBannerDismissed = false,
                previewEdition = null,
            )
        }

        dependencies.launch {
            dependencies.getEditionsByBookIdUseCase(
                bookId = bookId,
                forceNetwork = true,
            )
                .onSuccess { editions ->
                    scope.setLocalVariables { it.copy(editionsLoadedForBookId = bookId) }
                    scope.setState { it.copy(editions = editions.toImmutableList()) }
                }
                .onFailure { throwable ->
                    AppLog.e("Failed to refetch editions after the ISBN lookup for book $bookId: $throwable")
                }
        }
    }
}
