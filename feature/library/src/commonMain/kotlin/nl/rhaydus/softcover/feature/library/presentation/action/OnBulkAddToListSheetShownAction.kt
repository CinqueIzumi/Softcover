package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class OnBulkAddToListSheetShownAction(
    private val shown: Boolean,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState {
            if (shown) {
                it.copy(isChooseListsSheetRequested = true)
            } else {
                it.copy(
                    chooseListsSheet = SheetSession.Closed,
                    isChooseListsSheetRequested = false,
                )
            }
        }
    }
}
