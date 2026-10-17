package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.collections.immutable.toPersistentSet
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.QuoteProgressAdvance
import nl.rhaydus.softcover.core.domain.model.progressAdvanceFor
import nl.rhaydus.softcover.core.uibinding.progress.toActionAt
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toPosition
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.QuoteDraft
import nl.rhaydus.toad.ActionScope

/**
 * Records the quote and, when the reader left the toggle on, moves their progress to its position. The two
 * writes run as separate jobs so a later progress mutation cannot cancel the quote and a failed progress
 * write cannot lose it.
 */
internal class OnSaveQuoteAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val draft: QuoteDraft = scope.currentState.quoteDraft ?: return
        val book: Book = scope.currentState.book ?: return
        val text = draft.text.trim()

        if (text.isEmpty()) return

        val position = draft.toPosition(book = book)
        val advance: QuoteProgressAdvance? = position
            ?.let { book.progressAdvanceFor(it) }
            ?.takeIf { draft.progressAdvanceEnabled }

        scope.setState { it.copy(quoteDraft = null) }

        if (advance != null) {
            advanceProgress(
                book = book,
                advance = advance,
                draft = draft,
                dependencies = dependencies,
                scope = scope,
            )
        }

        dependencies.launch {
            dependencies.addQuoteUseCase(
                bookId = book.id,
                editionId = book.currentEdition?.id,
                text = text,
                privacy = draft.privacy,
                actionAt = draft.readAt?.toInstant(TimeZone.currentSystemDefault()),
                position = position,
                bookTitle = book.title,
            )
                .onFailure { error ->
                    AppLog.e("Failed to save quote: $error")

                    SnackBarManager.showSnackbar(title = "Couldn't save your quote. Try again.")

                    scope.sendEvent(BookDetailEvent.MutationFailedEvent())
                }
        }
    }

    private suspend fun advanceProgress(
        book: Book,
        advance: QuoteProgressAdvance,
        draft: QuoteDraft,
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.currentLocalVariables.bookMutationJobs[book.id]?.cancel()

        val job = dependencies.launch {
            dependencies.recordBookProgressUseCase(
                book = book,
                newPage = advance.newPage,
                newSeconds = advance.newSeconds,
                actionAt = draft.readAt.toActionAt(),
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
}
