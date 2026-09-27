package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.libraryLayoutChipForKey
import nl.rhaydus.softcover.feature.library.presentation.state.showsTitles
import nl.rhaydus.softcover.feature.library.presentation.state.toLayout
import nl.rhaydus.toad.ActionScope

/** A layout chip tap in the open Arrange sheet — mutates [LibraryUiState.arrangeDraft] only. */
internal class OnArrangeLayoutChipClickedAction(
    private val key: String,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        val chip = libraryLayoutChipForKey(key = key) ?: return

        scope.setState { state ->
            val draft = state.arrangeDraft ?: return@setState state

            state.copy(
                arrangeDraft = draft.copy(
                    gridLayout = chip.toLayout(showTitles = draft.gridLayout.showsTitles),
                ),
            )
        }
    }
}
