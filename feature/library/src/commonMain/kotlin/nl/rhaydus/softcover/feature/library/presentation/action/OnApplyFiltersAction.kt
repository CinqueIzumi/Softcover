package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Commits a filter-sheet draft: the UI builds [filters] locally (starting from
 * `state.filtersFor(tabId)`) while the sheet is open and dispatches this once, on "Show N titles" —
 * dismissing the sheet without tapping it discards the draft, since nothing was ever written to
 * state. A single atomic [scope.setState] write is enough to drive the same downstream derivation
 * [OnToggleFilterValueAction] and [OnClearFiltersAction] trigger (`DisplayListsCollector` reacts to
 * any `filtersByTab` change).
 */
internal class OnApplyFiltersAction(
    private val tabId: String,
    private val filters: LibraryFilters,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { state ->
            val newFilters = if (filters.isEmpty) {
                state.filtersByTab - tabId
            } else {
                state.filtersByTab + (tabId to filters)
            }

            state.copy(filtersByTab = newFilters)
        }
    }
}
