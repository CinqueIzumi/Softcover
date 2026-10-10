package nl.rhaydus.softcover.feature.scan.presentation.action

import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.toad.ActionScope

internal class OnAddUnknownIsbnDismissedAction : ScanAction {
    override suspend fun execute(
        dependencies: ScanDependencies,
        scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>,
    ) {
        if (scope.currentState.isAddingBook) return

        val resolved = scope.currentState.resolvedUnknownIsbn

        scope.setState {
            it.copy(
                unknownIsbnSheet = null,
                isUnknownIsbnCloseRequested = false,
                resolvedUnknownIsbn = null,
            )
        }

        if (resolved != null) {
            scope.sendEvent(
                event = ScanEvent.BookResolvedEvent(
                    book = resolved.book,
                    editionId = resolved.editionId,
                ),
            )
        }
    }
}
