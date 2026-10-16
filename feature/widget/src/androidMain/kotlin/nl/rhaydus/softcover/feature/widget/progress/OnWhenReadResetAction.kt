package nl.rhaydus.softcover.feature.widget.progress

import nl.rhaydus.toad.ActionScope

internal class OnWhenReadResetAction : UpdateProgressAction {
    override suspend fun execute(
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        scope.setState { it.copy(readAt = null) }
    }
}
