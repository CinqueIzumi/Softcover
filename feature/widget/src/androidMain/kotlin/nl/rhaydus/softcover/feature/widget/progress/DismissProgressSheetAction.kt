package nl.rhaydus.softcover.feature.widget.progress

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.toad.ActionScope

internal data object DismissProgressSheetAction : UpdateProgressAction {
    override suspend fun execute(
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        // The write must outlive the sheet's exit animation; finishing the activity clears the screen model.
        scope.currentLocalVariables.mutationJob?.join()

        val finishedBookId = scope.currentLocalVariables.finishedBookId
        val writeFailed = scope.currentLocalVariables.writeFailed

        scope.setState {
            it.copy(
                bookToUpdate = null,
                progressSheet = SheetSession.Closed,
                progressTimeDraft = null,
            )
        }

        if (writeFailed) {
            dependencies.showSaveFailed()
        } else {
            finishedBookId?.let(dependencies::openVerdict)
        }

        scope.sendEvent(UpdateProgressEvent.Finished)
    }
}
