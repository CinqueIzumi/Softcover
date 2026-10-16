package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.uibinding.progress.toActionAt
import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.ActionScope

internal data object OnMarkAsReadAction : UpdateProgressAction {
    override suspend fun execute(
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        val book = scope.currentState.bookToUpdate ?: return
        val actionAt = scope.currentState.readAt.toActionAt()

        scope.currentLocalVariables.mutationJob?.cancel()
        scope.setLocalVariables {
            it.copy(
                finishedBookId = null,
                writeFailed = false,
            )
        }

        val job = dependencies.launch {
            dependencies.markBookAsRead(
                book = book,
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
}
