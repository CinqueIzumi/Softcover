package nl.rhaydus.softcover.feature.scan.presentation.action

import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.toad.ActionScope

internal data object OnIsbnShakeEndedAction : ScanAction {
    override suspend fun execute(
        dependencies: ScanDependencies,
        scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>,
    ) {
        scope.setState {
            it.copy(isbnField = it.isbnField.copy(error = it.isbnField.error?.copy(shaking = false)))
        }
    }
}
