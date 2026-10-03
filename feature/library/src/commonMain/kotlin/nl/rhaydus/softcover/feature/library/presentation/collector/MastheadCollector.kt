package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * The only writer of [LibraryUiState.mastheads], [LibraryUiState.fallbackMasthead],
 * [LibraryUiState.loadingMastheadTabIds] and [LibraryUiState.mastheadRefresh] — one masthead per visible
 * tab, derived from that tab's stats and [LibraryUiState.isLoading]. Both the desktop and mobile mastheads
 * resolve their tab's entry through [LibraryUiState.mastheadFor] rather than building any copy themselves.
 */
internal class MastheadCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                MastheadSnapshot(
                    visibleTabs = state.visibleTabs,
                    tabStatsByTab = state.visibleTabs.mapNotNull { tab ->
                        state.tabStatsByTab[tab.id]?.let { stats -> tab.id to stats }
                    }.toMap(),
                    isLoading = state.isLoading,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        mastheads = snapshot.mastheads(),
                        fallbackMasthead = snapshot.fallbackMasthead(),
                        loadingMastheadTabIds = snapshot.loadingTabIds(),
                        mastheadRefresh = snapshot.refresh(),
                    )
                }
            }
    }
}
