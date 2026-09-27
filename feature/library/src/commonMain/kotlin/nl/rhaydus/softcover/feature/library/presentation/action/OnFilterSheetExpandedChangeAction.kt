package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Opens or closes the Filter sheet. Opening seeds [LibraryUiState.filterDraft] off the currently
 * selected tab's committed filters so the sheet always starts in sync; closing (scrim, back, or
 * after [OnApplyFiltersAction] commits) clears it, discarding anything the sheet's chips edited.
 */
internal class OnFilterSheetExpandedChangeAction(
    private val expanded: Boolean,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { state ->
            state.copy(
                isFilterSheetExpanded = expanded,
                filterDraft = if (expanded) {
                    LibraryFilterDraft(
                        tabId = state.selectedTabId,
                        filters = state.filtersFor(tabId = state.selectedTabId),
                    )
                } else {
                    null
                },
            )
        }
    }
}
