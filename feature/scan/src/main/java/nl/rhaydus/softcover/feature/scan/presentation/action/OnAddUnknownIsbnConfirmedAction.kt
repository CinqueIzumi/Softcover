package nl.rhaydus.softcover.feature.scan.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.feature.scan.presentation.event.AddBookFailedEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.BookResolvedEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState

internal class OnAddUnknownIsbnConfirmedAction : ScanAction {
    override suspend fun execute(
        dependencies: ScanDependencies,
        scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>,
    ) {
        val isbn = scope.currentState.unknownIsbn ?: return

        if (scope.currentState.isAddingBook) return

        scope.setState { it.copy(isAddingBook = true) }

        dependencies.addBookByIsbnUseCase(isbn = isbn)
            .onSuccess { result ->
                scope.setState { it.copy(
                    unknownIsbn = null,
                    isAddingBook = false,
                ) }

                scope.sendEvent(
                    event = BookResolvedEvent(
                        book = result.book,
                        editionId = result.editionId,
                    ),
                )
            }
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Failed to add scanned ISBN $isbn to Hardcover",
                )

                scope.setState { it.copy(isAddingBook = false) }

                scope.sendEvent(event = AddBookFailedEvent())
            }
    }
}
