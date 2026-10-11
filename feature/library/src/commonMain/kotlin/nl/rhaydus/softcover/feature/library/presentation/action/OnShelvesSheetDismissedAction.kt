package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
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
        val pending = scope.currentState.shelvesSheet.dismissed()

        scope.setState {
            it.copy(
                shelvesSheet = SheetSession.Closed,
                isShelvesSheetRequested = false,
                isShelvesSheetPickPending = false,
            )
        }

        if (pending != null) {
            scope.sendEvent(event = LibraryEvent.ManageShelvesRequestedEvent())
        }
    }
}
