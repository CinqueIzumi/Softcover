package nl.rhaydus.softcover.feature.scan.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.books.domain.usecase.IsbnLookupResult
import nl.rhaydus.softcover.feature.scan.presentation.event.BookResolvedEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.ResolutionFailedEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.UnknownEditionEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import timber.log.Timber

class OnIsbnSubmittedAction(
    val isbn: String,
) : ScanAction {
    override suspend fun execute(
        dependencies: ScanDependencies,
        scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>,
    ) {
        if (scope.currentState.isResolving) return

        scope.setState { it.copy(isResolving = true) }

        dependencies.resolveBookByIsbnUseCase(isbn = isbn)
            .onSuccess { result ->
                scope.setState { it.copy(isResolving = false) }

                when (result) {
                    is IsbnLookupResult.Found -> scope.sendEvent(
                        event = BookResolvedEvent(
                            book = result.book,
                            editionId = result.editionId,
                        ),
                    )

                    is IsbnLookupResult.UnknownEdition -> scope.sendEvent(event = UnknownEditionEvent())
                }
            }
            .onFailure { error ->
                Timber.e(error, "Failed to resolve scanned ISBN $isbn")

                scope.setState { it.copy(isResolving = false) }

                scope.sendEvent(event = ResolutionFailedEvent())
            }
    }
}
