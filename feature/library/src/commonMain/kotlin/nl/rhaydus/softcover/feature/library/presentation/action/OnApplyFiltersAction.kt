package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Commits [LibraryUiState.filterDraft], dispatched once on "Show N titles" — dismissing the sheet
 * without tapping it discards the draft, since nothing was ever written to state. A no-op if the
 * draft is already gone (the sheet closed between the tap and this action running). A single atomic
 * [scope.setState] write is enough to drive the same downstream derivation `OnToggleFilterValueAction`
 * and `OnClearFiltersAction` trigger (`DisplayListsCollector` reacts to any `filtersByTab` change).
 */
internal class OnApplyFiltersAction : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        val draft = scope.currentState.filterDraft ?: return

        scope.setState { state ->
            val newFilters = if (draft.filters.isEmpty) {
                state.filtersByTab - draft.tabId
            } else {
                state.filtersByTab + (draft.tabId to draft.filters)
            }

            state.copy(filtersByTab = newFilters)
        }
    }
}
