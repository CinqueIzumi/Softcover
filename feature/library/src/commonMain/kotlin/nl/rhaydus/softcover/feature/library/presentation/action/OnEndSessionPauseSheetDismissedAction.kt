package nl.rhaydus.softcover.feature.library.presentation.action

import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.session.stopSessionThen
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class OnEndSessionPauseSheetDismissedAction : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        val confirmed = scope.currentState.endSessionPauseSheet.dismissed()

        scope.setState {
            it.copy(
                endSessionPauseSheet = SheetSession.Closed,
                endSessionPauseBookIds = persistentSetOf(),
            )
        }

        if (confirmed == null) return

        dependencies.activeSessionController.stopSessionThen {
            OnBulkMoveShelfAction(
                status = UserBookStatus.PAUSED,
                explicitBookIds = confirmed.bookIds,
                sessionPauseConfirmed = true,
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )
        }
    }
}
