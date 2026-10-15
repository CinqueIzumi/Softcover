package nl.rhaydus.softcover.feature.scan.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.domain.exception.BookImportPendingException
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ResolvedScan
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.toad.ActionScope

internal class OnAddUnknownIsbnConfirmedAction : ScanAction {
    override suspend fun execute(
        dependencies: ScanDependencies,
        scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>,
    ) {
        val isbn = scope.currentState.unknownIsbn ?: return

        if (scope.currentState.isAddingBook) return

        scope.setState {
            it.copy(
                isAddingBook = true,
                isImportPending = false,
            )
        }

        dependencies.addBookByIsbnUseCase(isbn = isbn)
            .onSuccess { result ->
                scope.setState {
                    it.copy(
                        isAddingBook = false,
                        isImportPending = false,
                        unknownIsbn = null,
                        unknownIsbnSheet = it.unknownIsbnSheet.requestClose(
                            pending = ResolvedScan(
                                book = result.book,
                                editionId = result.editionId,
                            ),
                        ),
                    )
                }
            }
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Failed to add scanned ISBN $isbn to Hardcover",
                )

                val isPending = error is BookImportPendingException

                scope.setState {
                    it.copy(
                        isAddingBook = false,
                        isImportPending = isPending,
                    )
                }

                if (isPending.not()) {
                    scope.sendEvent(event = ScanEvent.AddBookFailedEvent())
                }
            }
    }
}
