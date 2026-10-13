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

internal class OnMarkBookAsReadingClickAction(private val book: Book) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.currentLocalVariables.bookMutationJobs[book.id]?.cancel()

        // A previewed or scanned edition (off-shelf) becomes the created user book's edition.
        val editionId = scope.currentState.previewEdition?.id ?: scope.currentState.scannedEditionId

        if (book.userBook == null) {
            scope.setState { it.copy(pendingShelfStatus = BookStatus.Reading) }
        }

        val job = dependencies.launch {
            dependencies.markBookAsReadingUseCase(
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
                            pendingShelfStatus = it.pendingShelfStatus.takeUnless { pending -> pending == BookStatus.Reading },
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
