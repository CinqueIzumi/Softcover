package nl.rhaydus.softcover.feature.library.presentation.action

import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.withSelectedBookIds
import nl.rhaydus.toad.ActionScope

internal class OnExitSelectionModeAction : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState {
            it.withSelectedBookIds(persistentSetOf()).copy(
                selectionMode = false,
                isBulkMoveMenuExpanded = false,
                isBulkRemoveSheetRequested = false,
                isChooseListsSheetRequested = false,
                listsBeingMutated = persistentSetOf(),
            )
        }
    }
}
