package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
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
            .map { state ->
                Triple(
                    state.selectedBookIds.size,
                    state.bulkActionInProgress,
                    state.isBulkRemoveSheetRequested,
                )
            }
            .distinctUntilChanged()
            .collectLatest { (bookCount, inProgress, requested) ->
                scope.setState {
                    it.copy(
                        bulkRemoveSheet = if (requested && it.isBulkRemoveSheetRequested) {
                            it.bulkRemoveSheet.openOrUpdate(
                                model = bulkRemoveSheetScaffoldFor(
                                    bookCount = bookCount,
                                    inProgress = inProgress,
                                ),
                            )
                        } else {
                            it.bulkRemoveSheet.requestClose()
                        },
                    )
                }
            }
    }
}
