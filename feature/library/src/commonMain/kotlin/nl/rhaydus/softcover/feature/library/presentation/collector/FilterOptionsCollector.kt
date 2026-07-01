package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.FilterOptionsInputs
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Precomputes [LibraryFilterOptions] for every visible tab so opening the filter sheet is an
 * O(1) lookup on the UI thread. The synchronous tag/format/year aggregation is otherwise paid
 * during composition the first time the sheet opens, which on large libraries stalls the slide-up
 * animation by enough to feel laggy.
 */
internal class FilterOptionsCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                FilterOptionsInputs(
                    booksByTab = state.booksByTab,
                    editionsByTab = state.editionsByTab,
                    bookByBookId = state.bookByBookId,
                )
            }
            .distinctUntilChanged()
            .collectLatest { inputs ->
                val options = withContext(dependencies.defaultDispatcher) {
                    inputs.compute()
                }

                scope.setState { it.copy(filterOptionsByTab = options) }
            }
    }
}
