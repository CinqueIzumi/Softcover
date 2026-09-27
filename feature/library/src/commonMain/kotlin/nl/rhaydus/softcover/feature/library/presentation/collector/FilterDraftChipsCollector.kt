package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Derives the Filter sheet's resolved facet chips (`component-contract.md` § 7.2 R9) off
 * [LibraryUiState.filterDraft] — recomputes on every draft edit (facet toggle, tag search
 * keystroke), and clears to `null` while the sheet is closed.
 */
internal class FilterDraftChipsCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                FilterDraftChipsSnapshot(
                    draft = state.filterDraft,
                    filterChipsByTab = state.filterChipsByTab,
                    booksByTab = state.booksByTab,
                    editionsByTab = state.editionsByTab,
                    addedAtByTab = state.addedAtByTab,
                    bookByBookId = state.bookByBookId,
                    sortModeByTab = state.sortModeByTab,
                    sortDirectionByTab = state.sortDirectionByTab,
                    searchQuery = state.searchQuery,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val selection = withContext(dependencies.defaultDispatcher) {
                    snapshot.compute()
                }

                scope.setState { it.copy(filterSheetSelection = selection) }
            }
    }
}
