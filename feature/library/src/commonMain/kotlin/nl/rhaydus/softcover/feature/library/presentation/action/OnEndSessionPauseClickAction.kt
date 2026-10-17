package nl.rhaydus.softcover.feature.library.presentation.action

import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.uibinding.session.ConfirmedPause
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class OnEndSessionPauseClickAction : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState {
            it.copy(
                endSessionPauseSheet = it.endSessionPauseSheet.requestClose(
                    pending = ConfirmedPause(bookIds = it.endSessionPauseBookIds),
                ),
                endSessionPauseBookIds = persistentSetOf(),
            )
        }
    }
}
