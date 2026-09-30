package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.bulkRemoveSheetScaffoldFor
import nl.rhaydus.toad.ActionScope

internal class BulkRemoveSheetCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state -> state.selectedBookIds.size to state.bulkActionInProgress }
            .distinctUntilChanged()
            .collectLatest { (bookCount, inProgress) ->
                scope.setState {
                    it.copy(
                        bulkRemoveSheet = bulkRemoveSheetScaffoldFor(
                            bookCount = bookCount,
                            inProgress = inProgress,
                        ),
                    )
                }
            }
    }
}
