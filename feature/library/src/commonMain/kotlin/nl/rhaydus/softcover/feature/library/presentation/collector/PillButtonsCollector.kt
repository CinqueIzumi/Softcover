package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibrarySelectionPills
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.filterPillButton
import nl.rhaydus.toad.ActionScope

/**
 * The only writer of [LibraryUiState.filterPillByTab] and [LibraryUiState.selectionPills]: the Filter
 * pill's active dot follows each tab's filters, and the selection header's pills follow
 * [LibraryUiState.bulkActionInProgress].
 */
internal class PillButtonsCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                PillButtonsSnapshot(
                    filterActiveByTab = state.visibleTabs.associate { tab ->
                        tab.id to state.filtersFor(tabId = tab.id).isEmpty.not()
                    },
                    bulkActionInProgress = state.bulkActionInProgress,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        filterPillByTab = snapshot.filterActiveByTab.mapValues { (_, active) ->
                            filterPillButton(active = active)
                        }.toImmutableMap(),
                        selectionPills = LibrarySelectionPills.of(
                            bulkActionInProgress = snapshot.bulkActionInProgress,
                        ),
                    )
                }
            }
    }
}
