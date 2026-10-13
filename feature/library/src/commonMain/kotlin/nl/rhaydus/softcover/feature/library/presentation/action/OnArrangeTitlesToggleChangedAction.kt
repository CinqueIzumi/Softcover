package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.withTitlesShown
import nl.rhaydus.toad.ActionScope

/** The "Show titles & authors" toggle in the open Arrange sheet — mutates [LibraryUiState.arrangeDraft] only. */
internal class OnArrangeTitlesToggleChangedAction(
    private val show: Boolean,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { state ->
            val draft = state.arrangeDraft ?: return@setState state

            state.copy(
                arrangeDraft = draft.copy(gridLayout = draft.gridLayout.withTitlesShown(show = show)),
            )
        }
    }
}
