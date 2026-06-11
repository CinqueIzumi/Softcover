package nl.rhaydus.softcover.feature.scan.presentation.action

import nl.rhaydus.softcover.core.book.domain.usecase.IsbnLookupResult
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.feature.scan.presentation.event.BookResolvedEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.InvalidIsbnEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.ResolutionFailedEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState

internal class OnIsbnSubmittedAction(
    val isbn: String,
) : ScanAction {
    override suspend fun execute(
        dependencies: ScanDependencies,
        scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>,
    ) {
        if (scope.currentState.isResolving || scope.currentState.isAddingBook) return

        scope.setState { it.copy(isResolving = true) }

        dependencies.resolveBookByIsbnUseCase(isbn = isbn)
            .onSuccess { result ->
                when (result) {
                    is IsbnLookupResult.Found -> {
                        scope.setState { it.copy(isResolving = false) }

                        scope.sendEvent(
                            event = BookResolvedEvent(
                                book = result.book,
                                editionId = result.editionId,
                            ),
                        )
                    }

                    is IsbnLookupResult.UnknownEdition -> {
                        scope.setState { it.copy(
                            isResolving = false,
                            unknownIsbn = result.normalizedIsbn,
                        ) }
                    }

                    is IsbnLookupResult.InvalidIsbn -> {
                        scope.setState { it.copy(isResolving = false) }

                        scope.sendEvent(event = InvalidIsbnEvent())
                    }
                }
            }
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Failed to resolve scanned ISBN $isbn",
                )

                scope.setState { it.copy(isResolving = false) }

                scope.sendEvent(event = ResolutionFailedEvent())
            }
    }
}
