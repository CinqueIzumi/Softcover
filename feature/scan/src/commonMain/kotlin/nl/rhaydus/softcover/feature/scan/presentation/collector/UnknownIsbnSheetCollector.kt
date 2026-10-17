package nl.rhaydus.softcover.feature.scan.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.softcover.feature.scan.presentation.state.UnknownIsbnSheetUiModel
import nl.rhaydus.softcover.feature.scan.presentation.state.unknownIsbnSheetScaffoldFor
import nl.rhaydus.toad.ActionScope

internal class UnknownIsbnSheetCollector : ScanCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>,
        dependencies: ScanDependencies,
    ) {
        scope.state
            .map { state -> Triple(
                    first = state.unknownIsbn,
                    second = state.isAddingBook,
                    third = state.isImportPending,
                ) }
            .distinctUntilChanged()
            .collectLatest { (isbn, isAdding, isImportPending) ->
                scope.setState {
                    it.copy(
                        unknownIsbnSheet = if (isbn == null || it.unknownIsbn == null) {
                            it.unknownIsbnSheet.requestClose()
                        } else {
                            it.unknownIsbnSheet.openOrUpdate(
                                model = UnknownIsbnSheetUiModel(
                                    isbn = isbn,
                                    scaffold = unknownIsbnSheetScaffoldFor(
                                        isAdding = isAdding,
                                        isImportPending = isImportPending,
                                    ),
                                ),
                            )
                        },
                    )
                }
            }
    }
}
