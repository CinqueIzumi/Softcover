package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.filterSheetScaffoldFor
import nl.rhaydus.toad.ActionScope

/**
 * Opens or closes the Filter sheet. Opening seeds [LibraryUiState.filterDraft] off the currently
 * selected tab's committed filters so the sheet always starts in sync; closing (scrim, back, or
 * after [OnApplyFiltersAction] commits) clears it, discarding anything the sheet's chips edited.
 * Opening also seeds [LibraryUiState.filterSheet]'s scaffold off the committed filters, so the footer is right
 * before `FilterDraftChipsCollector` has produced the first live selection.
 */
internal class OnFilterSheetExpandedChangeAction(
    private val expanded: Boolean,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { state ->
            val filters = state.filtersFor(tabId = state.selectedTabId)

            state.copy(
                isFilterSheetExpanded = expanded,
                filterSheet = state.filterSheet.copy(
                    scaffold = if (expanded) {
                        filterSheetScaffoldFor(
                            resultCount = state.tabStatsFor(tabId = state.selectedTabId).itemCount,
                            filtersActive = filters.isEmpty.not(),
                        )
                    } else {
                        state.filterSheet.scaffold
                    },
                ),
                filterDraft = if (expanded) {
                    LibraryFilterDraft(
                        tabId = state.selectedTabId,
                        filters = filters,
                    )
                } else {
                    null
                },
            )
        }
    }
}
