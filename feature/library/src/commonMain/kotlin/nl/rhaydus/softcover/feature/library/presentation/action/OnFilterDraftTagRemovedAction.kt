package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.tagValueFor
import nl.rhaydus.softcover.feature.library.presentation.state.withoutTag
import nl.rhaydus.toad.ActionScope

/** The remove control on a chosen tag chip in the open Filter sheet; clears the tag from [LibraryUiState.filterDraft] only. */
internal class OnFilterDraftTagRemovedAction(
    private val key: String,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { state ->
            val draft = state.filterDraft ?: return@setState state
            val value = state.availableFilterOptionsFor(draft.tabId).tagValueFor(
                key = key,
                filters = draft.filters,
            ) ?: return@setState state

            state.copy(filterDraft = draft.copy(filters = draft.filters.withoutTag(value = value)))
        }
    }
}
