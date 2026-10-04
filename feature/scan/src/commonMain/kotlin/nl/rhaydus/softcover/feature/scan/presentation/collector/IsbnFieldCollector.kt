package nl.rhaydus.softcover.feature.scan.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.toad.ActionScope

/**
 * Derives only `enabled`; the text stays written by `OnIsbnChangedAction`, so a keystroke never
 * waits on this collector.
 */
internal class IsbnFieldCollector : ScanCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>,
        dependencies: ScanDependencies,
    ) {
        scope.state
            .map { state -> state.isResolving || state.isAddingBook }
            .distinctUntilChanged()
            .collectLatest { busy ->
                scope.setState { it.copy(isbnField = it.isbnField.copy(enabled = busy.not())) }
            }
    }
}
