package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

/**
 * Desktop Shift-click range select: adds every book in [bookIds] (the visible span between the
 * selection anchor and the shift-clicked cover) to the selection, entering selection mode if it is
 * not already active. Selection and rearrange are mutually exclusive, so entering selection clears
 * rearrange. A no-op when [bookIds] is empty.
 */
internal class OnSelectBookRangeAction(
    private val bookIds: List<Int>,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        if (bookIds.isEmpty()) return

        scope.setState {
            it.copy(
                selectionMode = true,
                selectedBookIds = it.selectedBookIds + bookIds,
                isRearranging = false,
            )
        }
    }
}
