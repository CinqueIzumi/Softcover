package nl.rhaydus.softcover.feature.widget.progress

import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.toad.ActionScope

internal data class LoadUpdateProgressAction(val bookId: Int) : UpdateProgressAction {
    override suspend fun execute(
        dependencies: UpdateProgressDependencies,
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
    ) {
        if (scope.currentState.bookToUpdate != null) return

        val isSignedIn = (dependencies.getUserIdAsFlow().firstOrNull() ?: NO_USER_ID) != NO_USER_ID

        val book = if (isSignedIn) {
            dependencies.getCurrentlyReadingUserBooks().firstOrNull()?.firstOrNull { it.id == bookId }
        } else {
            null
        }

        if (book == null) {
            scope.sendEvent(UpdateProgressEvent.Finished)

            return
        }

        scope.setState {
            it.copy(
                bookToUpdate = book,
                readAt = null,
            )
        }
    }
}
