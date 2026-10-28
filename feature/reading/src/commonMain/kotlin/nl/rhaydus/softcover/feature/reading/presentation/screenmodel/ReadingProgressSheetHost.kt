package nl.rhaydus.softcover.feature.reading.presentation.screenmodel

import kotlinx.collections.immutable.toPersistentSet
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressEntry
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetHost
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetState
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal object ReadingProgressSheetHost : ProgressSheetHost<
    ReadingScreenUiState,
    ReadingScreenEvent,
    ReadingScreenDependencies,
    ReadingLocalVariables,
    > {
    override fun bookOf(state: ReadingScreenUiState): Book? = state.bookToUpdate

    override fun sheetOf(state: ReadingScreenUiState): ProgressSheetState = state.progressSheet

    override fun withSheet(
        state: ReadingScreenUiState,
        sheet: ProgressSheetState,
    ): ReadingScreenUiState = state.copy(progressSheet = sheet)

    override suspend fun record(
        book: Book,
        entry: ProgressEntry,
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
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
                        scope.setState {
                            it.copy(
                                verdictPromptBook = book,
                            )
                        }
                    } else {
                        scope.sendEvent(ReadingScreenEvent.ProgressUpdatedEvent())
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
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        scope.sendEvent(
            ReadingScreenEvent.MarkAsReadRequestedEvent(
                book = book,
                actionAt = actionAt,
            ),
        )
    }

    override suspend fun awaitPendingWrite(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) = Unit

    override suspend fun afterClose(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        scope.setState { it.copy(bookToUpdate = null) }
    }
}
