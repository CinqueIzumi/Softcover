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

internal class RearrangeChipCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                RearrangeChipSnapshot(
                    visibleTabs = state.visibleTabs,
                    sortModeByTab = state.sortModeByTab,
                    displayBookCountByTab = state.displayBooksByTab
                        .filterKeys { it in state.booksByTab }
                        .mapValues { it.value.size },
                    displayEditionCountByTab = state.displayEditionsByTab
                        .filterKeys { it in state.editionsByTab }
                        .mapValues { it.value.size },
                    rankedCustomListIds = state.customLists.filter { it.ranked }.mapTo(mutableSetOf()) { it.id },
                    isRearranging = state.isRearranging,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val chipByTab = withContext(dependencies.defaultDispatcher) {
                    snapshot.compute()
                }

                scope.setState { it.copy(rearrangeChipByTab = chipByTab) }
            }
    }
}
