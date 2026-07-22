package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Enters bulk-selection mode. [bookId], when supplied, seeds the selection with that one book — the
 * long-press-on-a-cover entry point, which should open with exactly the pressed cover already ticked.
 * Left `null` for the toolbar Select-circle entry point (the masthead control line), which should
 * open with nothing pre-selected — the user picks their own starting point.
 */
internal class OnEnterSelectionModeAction(
    private val bookId: Int? = null,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        // Selection and rearrange are mutually exclusive editing modes — entering one clears the other.
        scope.setState {
            it.copy(
                selectionMode = true,
                selectedBookIds = bookId?.let { id -> setOf(id) } ?: emptySet(),
                isRearranging = false,
            )
        }
    }
}
