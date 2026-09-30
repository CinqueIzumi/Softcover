package nl.rhaydus.softcover.feature.scan.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.softcover.feature.scan.presentation.state.unknownIsbnSheetScaffoldFor
import nl.rhaydus.toad.ActionScope

internal class UnknownIsbnSheetCollector : ScanCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>,
        dependencies: ScanDependencies,
    ) {
        scope.state
            .map { state -> state.isAddingBook }
            .distinctUntilChanged()
            .collectLatest { isAdding ->
                scope.setState {
                    it.copy(
                        unknownIsbnSheet = unknownIsbnSheetScaffoldFor(isAdding = isAdding),
                    )
                }
            }
    }
}
