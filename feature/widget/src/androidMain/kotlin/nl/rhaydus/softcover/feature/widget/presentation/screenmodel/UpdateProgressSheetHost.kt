package nl.rhaydus.softcover.feature.widget.presentation.screenmodel

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressEntry
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetHost
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetState
import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.ActionScope

internal object UpdateProgressSheetHost :
    ProgressSheetHost<
        UpdateProgressUiState,
        UpdateProgressEvent,
        UpdateProgressDependencies,
        UpdateProgressLocalVariables,
        > {
    override fun bookOf(state: UpdateProgressUiState): Book? = state.bookToUpdate

    override fun sheetOf(state: UpdateProgressUiState): ProgressSheetState = state.progressSheet

    override fun withSheet(
        state: UpdateProgressUiState,
        sheet: ProgressSheetState,
    ): UpdateProgressUiState = state.copy(progressSheet = sheet)

    override suspend fun record(
        book: Book,
        entry: ProgressEntry,
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        launchWrite(
            book = book,
            dependencies = dependencies,
            scope = scope,
        ) {
            dependencies.recordBookProgress(
                book = book,
                newPage = entry.newPage,
                newSeconds = entry.newSeconds,
                actionAt = entry.actionAt,
            )
        }
    }

    override suspend fun markAsRead(
        book: Book,
        actionAt: String?,
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        launchWrite(
            book = book,
            dependencies = dependencies,
            scope = scope,
        ) {
            dependencies.markBookAsRead(
                book = book,
                actionAt = actionAt,
            )
        }
    }

    override suspend fun awaitPendingWrite(
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        // The write must outlive the sheet's exit animation; finishing the activity clears the screen model.
        scope.currentLocalVariables.mutationJob?.join()
    }

    override suspend fun afterClose(
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        val finishedBookId = scope.currentLocalVariables.finishedBookId
        val writeFailed = scope.currentLocalVariables.writeFailed

        if (writeFailed) {
            dependencies.showSaveFailed()
        } else {
            finishedBookId?.let(dependencies::openVerdict)
        }

        scope.sendEvent(UpdateProgressEvent.Finished)
    }

    private fun launchWrite(
        book: Book,
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
        write: suspend () -> Result<ShelfMutationOutcome?>,
    ) {
        scope.currentLocalVariables.mutationJob?.cancel()

        scope.setLocalVariables {
            it.copy(
                finishedBookId = null,
                writeFailed = false,
            )
        }

        val job = dependencies.launch {
            write()
                .onSuccess { outcome ->
                    scope.setLocalVariables {
                        it.copy(finishedBookId = book.id.takeIf { outcome == ShelfMutationOutcome.Applied })
                    }
                }
                .onFailure { error ->
                    scope.setLocalVariables { it.copy(writeFailed = true) }

                    AppLog.e("$error")
                }
        }

        scope.setLocalVariables { it.copy(mutationJob = job) }
    }
}
