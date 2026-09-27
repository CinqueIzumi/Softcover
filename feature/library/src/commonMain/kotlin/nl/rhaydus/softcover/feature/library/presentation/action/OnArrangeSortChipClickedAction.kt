package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.librarySortModeForKey
import nl.rhaydus.toad.ActionScope

/**
 * A sort chip tap in the open Arrange sheet — mutates [LibraryUiState.arrangeDraft] only. Tapping
 * the already-active mode flips its direction; tapping a different mode switches to it at its
 * default direction.
 */
internal class OnArrangeSortChipClickedAction(
    private val key: String,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        val mode = librarySortModeForKey(key = key) ?: return

        scope.setState { state ->
            val draft = state.arrangeDraft ?: return@setState state

            val nextDraft = if (mode == draft.sortMode) {
                draft.copy(sortDirection = draft.sortDirection.flipped())
            } else {
                draft.copy(
                    sortMode = mode,
                    sortDirection = mode.defaultDirection,
                )
            }

            state.copy(arrangeDraft = nextDraft)
        }
    }
}
