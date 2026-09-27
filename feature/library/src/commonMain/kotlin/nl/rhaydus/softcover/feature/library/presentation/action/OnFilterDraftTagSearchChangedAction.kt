package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/** The open Filter sheet's tag search field — mutates [LibraryUiState.filterDraft] only. */
internal class OnFilterDraftTagSearchChangedAction(
    private val query: String,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { state ->
            val draft = state.filterDraft ?: return@setState state

            state.copy(filterDraft = draft.copy(tagSearch = query))
        }
    }
}
