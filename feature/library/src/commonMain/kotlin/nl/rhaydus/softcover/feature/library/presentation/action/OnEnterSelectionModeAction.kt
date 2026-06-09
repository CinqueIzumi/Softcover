package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

internal class OnEnterSelectionModeAction(
    private val bookId: Int,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        // Selection and rearrange are mutually exclusive editing modes — entering one clears the other.
        scope.setState {
            it.copy(
                selectionMode = true,
                selectedBookIds = setOf(bookId),
                isRearranging = false,
            )
        }
    }
}
