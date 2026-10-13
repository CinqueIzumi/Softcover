package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.toggle
import nl.rhaydus.toad.ActionScope

/** A facet chip tap in the open Filter sheet — mutates [LibraryUiState.filterDraft] only. */
internal class OnFilterDraftChipToggledAction(
    private val key: String,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { state ->
            val draft = state.filterDraft ?: return@setState state
            val value = state.filterChipsFor(draft.tabId)[key] ?: return@setState state

            state.copy(filterDraft = draft.copy(filters = draft.filters.toggle(value = value)))
        }
    }
}
