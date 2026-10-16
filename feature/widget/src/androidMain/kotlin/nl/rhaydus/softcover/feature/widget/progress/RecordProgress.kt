package nl.rhaydus.softcover.feature.widget.progress

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.progress.toActionAt
import nl.rhaydus.toad.ActionScope

internal fun recordProgress(
    dependencies: UpdateProgressDependencies,
    scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    book: Book,
    newPage: Int?,
    newSeconds: Int?,
) {
    val actionAt = scope.currentState.readAt.toActionAt()

    scope.currentLocalVariables.mutationJob?.cancel()
    scope.setLocalVariables {
        it.copy(
            finishedBookId = null,
            writeFailed = false,
        )
    }

    val job = dependencies.launch {
        dependencies.recordBookProgress(
            book = book,
            newPage = newPage,
            newSeconds = newSeconds,
            actionAt = actionAt,
        )
            .onSuccess { outcome ->
                scope.setLocalVariables {
                    it.copy(finishedBookId = book.id.takeIf { outcome == ShelfMutationOutcome.Applied })
                }

                dependencies.refreshWidget()
            }
            .onFailure { error ->
            scope.setLocalVariables { it.copy(writeFailed = true) }

            AppLog.e("$error")
        }
    }

    scope.setLocalVariables { it.copy(mutationJob = job) }
}
