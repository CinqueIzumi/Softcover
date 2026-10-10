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
            .map { state -> state.isAddingBook to state.isUnknownIsbnCloseRequested }
            .distinctUntilChanged()
            .collectLatest { (isAdding, closeRequested) ->
                scope.setState {
                    val current = it.unknownIsbnSheet

                    if (current == null) {
                        it
                    } else {
                        // Keep the footer as it was while the sheet hides, instead of flashing back to idle.
                        val scaffold = if (closeRequested) {
                            current.scaffold.copy(closeRequested = true)
                        } else {
                            unknownIsbnSheetScaffoldFor(isAdding = isAdding)
                        }

                        it.copy(unknownIsbnSheet = current.copy(scaffold = scaffold))
                    }
                }
            }
    }
}
