package nl.rhaydus.softcover.feature.scan.presentation.action

import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.toad.ActionScope

internal class OnIsbnChangedAction(
    private val text: String,
) : ScanAction {
    override suspend fun execute(
        dependencies: ScanDependencies,
        scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>,
    ) {
        scope.setState { it.copy(isbnField = it.isbnField.copy(text = text)) }
    }
}
