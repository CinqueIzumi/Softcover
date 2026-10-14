package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.collections.immutable.toPersistentSet
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnMarkBookAsPausedClickAction(
    private val book: Book,
    private val sessionPauseConfirmed: Boolean = false,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        if (sessionPauseConfirmed.not() && scope.currentState.activeSessionBookId == book.id) {
            scope.setState { it.copy(endSessionPauseRequested = true) }

            return
        }

        scope.currentLocalVariables.bookMutationJobs[book.id]?.cancel()

        val editionId = scope.currentState.previewEdition?.id ?: scope.currentState.scannedEditionId

        if (book.userBook == null) {
            scope.setState { it.copy(pendingShelfStatus = BookStatus.Paused) }
        }

        val job = dependencies.launch {
            dependencies.pauseBookUseCase(
                book = book,
                editionId = editionId,
            )
                .onSuccess {
                    scope.sendEvent(BookDetailEvent.ShelfChangedEvent())
                }
                .onFailure { error ->
                    AppLog.e("$error")

                    scope.setState {
                        it.copy(
                            pendingShelfStatus = it.pendingShelfStatus.takeUnless { pending -> pending == BookStatus.Paused },
                            failedMutationBookIds = it.failedMutationBookIds.toPersistentSet().add(book.id),
                        )
                    }
                }
        }

        scope.setLocalVariables {
            it.copy(bookMutationJobs = it.bookMutationJobs + (book.id to job))
        }
    }
}
