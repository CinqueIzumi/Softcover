package nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel

import kotlinx.collections.immutable.toPersistentSet
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressEntry
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetHost
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetState
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsReadClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal object BookDetailProgressSheetHost :
    ProgressSheetHost<BookDetailUiState, BookDetailEvent, BookDetailDependencies, BookDetailLocalVariables> {
    override fun bookOf(state: BookDetailUiState): Book? = state.book

    override fun sheetOf(state: BookDetailUiState): ProgressSheetState = state.progressSheet

    override fun withSheet(
        state: BookDetailUiState,
        sheet: ProgressSheetState,
    ): BookDetailUiState = state.copy(progressSheet = sheet)

    override suspend fun record(
        book: Book,
        entry: ProgressEntry,
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.currentLocalVariables.bookMutationJobs[book.id]?.cancel()

        val job = dependencies.launch {
            dependencies.recordBookProgressUseCase(
                book = book,
                newPage = entry.newPage,
                newSeconds = entry.newSeconds,
                actionAt = entry.actionAt,
            )
                .onSuccess { outcome ->
                    if (outcome == ShelfMutationOutcome.Applied) {
                        scope.sendEvent(BookDetailEvent.BookMarkedAsReadEvent())
                    } else {
                        scope.sendEvent(BookDetailEvent.ProgressUpdatedEvent())
                    }
                }
                .onFailure { error ->
                    AppLog.e("$error")

                    scope.setState {
                        it.copy(failedMutationBookIds = it.failedMutationBookIds.toPersistentSet().add(book.id))
                    }
                }
        }

        scope.setLocalVariables {
            it.copy(bookMutationJobs = it.bookMutationJobs + (book.id to job))
        }
    }

    override suspend fun markAsRead(
        book: Book,
        actionAt: String?,
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        OnMarkBookAsReadClickAction(
            book = book,
            actionAt = actionAt,
        ).execute(
            dependencies = dependencies,
            scope = scope,
        )
    }

    override suspend fun awaitPendingWrite(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) = Unit

    override suspend fun afterClose(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) = Unit
}
