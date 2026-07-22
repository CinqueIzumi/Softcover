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

internal data class OnUpdatePercentageProgressClickAction(
    val newPercentage: String,
    val actionAt: String? = null,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val bookToUpdate: Book = scope.currentState.book ?: return

        val newPercentageValue: Double = newPercentage.toDoubleOrNull() ?: 0.0
        val fraction = (newPercentageValue / 100.0).coerceIn(
            0.0,
            1.0,
        )

        val edition = bookToUpdate.currentEdition ?: run {
            scope.setState { it.copy(showUpdateProgressSheet = false) }
            return
        }
        val isAudiobook = edition.isAudiobook

        val newPage: Int? = if (isAudiobook) {
            null
        } else {
            val total = edition.pages ?: bookToUpdate.defaultEdition?.pages ?: 0
            (fraction * total).toInt()
        }

        val newSeconds: Int? = if (isAudiobook) {
            val total = edition.audioSeconds ?: 0
            (fraction * total).toInt()
        } else {
            null
        }

        scope.currentLocalVariables.bookMutationJobs[bookToUpdate.id]?.cancel()

        val job = dependencies.launch {
            dependencies.recordBookProgressUseCase(
                book = bookToUpdate,
                newPage = newPage,
                newSeconds = newSeconds,
                actionAt = actionAt,
            )
                .onSuccess { outcome ->
                    // Only a genuine finish transition raises the verdict prompt — re-recording the
                    // last position on an already-read book returns NoChange and must stay silent.
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
