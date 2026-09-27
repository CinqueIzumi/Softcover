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
 * Derives the Arrange sheet's layout and sort chip models (`component-contract.md` § 7.2 R9) off
 * [LibraryUiState.arrangeDraft] — recomputes on every draft edit, and produces empty rows while the
 * sheet is closed (`draft == null`).
 */
internal class ArrangeDraftChipsCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                ArrangeDraftChipsSnapshot(
                    draft = state.arrangeDraft,
                    visibleTabs = state.visibleTabs,
                    customLists = state.customLists,
                    booksByTab = state.booksByTab,
                    deadlines = state.deadlines,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val (layoutChips, sortChips) = withContext(dependencies.defaultDispatcher) {
                    snapshot.compute()
                }

                scope.setState {
                    it.copy(
                        arrangeLayoutChips = layoutChips,
                        arrangeSortChips = sortChips,
                    )
                }
            }
    }
}
