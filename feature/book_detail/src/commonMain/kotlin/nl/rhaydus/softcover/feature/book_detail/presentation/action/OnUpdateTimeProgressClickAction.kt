package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookMarkedAsReadEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal data class OnUpdateTimeProgressClickAction(
    val hours: String,
    val minutes: String,
    val seconds: String,
    val actionAt: String? = null,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val bookToUpdate: Book = scope.currentState.book ?: return

        val h = hours.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val m = minutes.toIntOrNull()?.coerceIn(
            0,
            59,
        ) ?: 0
        val s = seconds.toIntOrNull()?.coerceIn(
            0,
            59,
        ) ?: 0

        val total = bookToUpdate.currentEdition?.audioSeconds ?: 0
        val entered = h * 3600 + m * 60 + s

        // With a known duration the entry is clamped to it; when the audiobook's length is unknown
        // there's no ceiling to clamp against, so any non-negative time is accepted as-is.
        val newSeconds = if (total > 0) {
            entered.coerceIn(
                0,
                total,
            )
        } else {
            entered.coerceAtLeast(0)
        }

        scope.currentLocalVariables.bookMutationJobs[bookToUpdate.id]?.cancel()

        val job = dependencies.launch {
            dependencies.recordBookProgressUseCase(
                book = bookToUpdate,
                newSeconds = newSeconds,
                actionAt = actionAt,
            )
                .onSuccess { outcome ->
                    // Only a genuine finish transition raises the verdict prompt — re-recording the
                    // full duration on an already-read book returns NoChange and must stay silent.
                    if (outcome == ShelfMutationOutcome.Applied) {
                        scope.sendEvent(BookMarkedAsReadEvent())
                    }
                }
                .onFailure { error ->
                    AppLog.e("$error")

                    scope.setState {
                        it.copy(failedMutationBookIds = it.failedMutationBookIds + bookToUpdate.id)
                    }
                }
        }

        scope.setLocalVariables {
            it.copy(bookMutationJobs = it.bookMutationJobs + (bookToUpdate.id to job))
        }

        scope.setState {
            it.copy(showUpdateProgressSheet = false)
        }
    }
}
