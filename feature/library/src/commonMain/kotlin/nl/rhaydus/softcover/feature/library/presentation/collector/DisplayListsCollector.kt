package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.DisplayInputs
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Precomputes the display lists (search + filter + sort applied) for every visible tab off
 * the main thread, so [LibraryUiState.displayBooksFor] and [LibraryUiState.displayEditionsFor] are
 * O(1) map lookups during composition. This eliminates the full-list `.filter {}` allocation that
 * previously ran on every scroll frame for large libraries.
 *
 * Mirrors [FilterOptionsCollector]: maps state to an inputs bundle, gates recomputation with
 * [distinctUntilChanged], and runs the work on [LibraryDependencies.defaultDispatcher]. The output
 * fields ([LibraryUiState.displayBooksByTab] etc.) are deliberately excluded from the inputs so a
 * self-write never triggers a recomputation loop.
 */
internal class DisplayListsCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        var prevBooksByTab: Map<String, List<Book>> = emptyMap()
        var prevEditionsByTab: Map<String, List<BookEdition>> = emptyMap()

        scope.state
            .map { state ->
                DisplayInputs(
                    booksByTab = state.booksByTab,
                    editionsByTab = state.editionsByTab,
                    searchQuery = state.searchQuery,
                    filtersByTab = state.filtersByTab,
                    sortModeByTab = state.sortModeByTab,
                    sortDirectionByTab = state.sortDirectionByTab,
                    addedAtByTab = state.addedAtByTab,
                    bookByBookId = state.bookByBookId,
                )
            }
            .distinctUntilChanged()
            .collectLatest { inputs ->
                val snapshotBooksByTab = prevBooksByTab
                val snapshotEditionsByTab = prevEditionsByTab

                val result = withContext(dependencies.defaultDispatcher) {
                    inputs.compute(
                        prevBooksByTab = snapshotBooksByTab,
                        prevEditionsByTab = snapshotEditionsByTab,
                    )
                }

                prevBooksByTab = result.displayBooksByTab
                prevEditionsByTab = result.displayEditionsByTab

                scope.setState { state ->
                    state.copy(
                        displayBooksByTab = result.displayBooksByTab,
                        displayEditionsByTab = result.displayEditionsByTab,
                        tabStatsByTab = result.tabStatsByTab,
                    )
                }
            }
    }
}
