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
 * Precomputes the Filter sheet's chip models for every tab (`component-contract.md` § 7.2 R9) off
 * [LibraryUiState.filterOptionsByTab], one step downstream of [FilterOptionsCollector] — so this only
 * recomputes when the *available* facet values change, never on a draft toggle inside the sheet
 * (that draft is composition-local and never touches [LibraryUiState]; see [LibraryFilterChips]'s
 * KDoc).
 */
internal class FilterChipModelsCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state -> FilterChipModelsSnapshot(filterOptionsByTab = state.filterOptionsByTab) }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val (chipsByTab, valueByKey) = withContext(dependencies.defaultDispatcher) {
                    snapshot.compute()
                }

                scope.setState {
                    it.copy(
                        filterChipsByTab = chipsByTab,
                        filterValueByChipKey = valueByKey,
                    )
                }
            }
    }
}
