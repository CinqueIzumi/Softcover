package nl.rhaydus.softcover.feature.widget.progress

import nl.rhaydus.toad.ActionScope

internal data class OnUpdateTimeProgressClickAction(
    val hours: String,
    val minutes: String,
    val seconds: String,
) : UpdateProgressAction {
    override suspend fun execute(
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        val book = scope.currentState.bookToUpdate ?: return

        val entered = (hours.toIntOrNull() ?: 0) * 3600 + (minutes.toIntOrNull() ?: 0) * 60 + (seconds.toIntOrNull() ?: 0)
        val total = book.currentEdition?.audioSeconds ?: 0

        val newSeconds = if (total > 0) {
            entered.coerceIn(
                0,
                total,
            )
        } else {
            entered.coerceAtLeast(0)
        }

        recordProgress(
            dependencies = dependencies,
            scope = scope,
            book = book,
            newPage = null,
            newSeconds = newSeconds,
        )
    }
}
