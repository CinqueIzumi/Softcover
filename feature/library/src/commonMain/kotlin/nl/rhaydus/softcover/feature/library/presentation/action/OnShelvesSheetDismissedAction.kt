package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class OnShelvesSheetDismissedAction : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        val isManageShelvesPending = scope.currentState.isManageShelvesPending

        scope.setState {
            it.copy(
                isShelvesSheetExpanded = false,
                isManageShelvesPending = false,
                isShelvesSheetPickPending = false,
                isShelvesSheetCloseRequested = false,
            )
        }

        if (isManageShelvesPending) {
            scope.sendEvent(event = LibraryEvent.ManageShelvesRequestedEvent())
        }
    }
}
