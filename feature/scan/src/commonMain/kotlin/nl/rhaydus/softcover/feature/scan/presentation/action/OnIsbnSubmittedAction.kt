package nl.rhaydus.softcover.feature.scan.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.IsbnLookupResult
import nl.rhaydus.softcover.core.component.field.TextFieldError
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.IsbnOrigin
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.softcover.feature.scan.presentation.state.UnknownIsbnSheetUiModel
import nl.rhaydus.softcover.feature.scan.presentation.state.unknownIsbnSheetScaffoldFor
import nl.rhaydus.toad.ActionScope

internal class OnIsbnSubmittedAction(
    val isbn: String,
    val origin: IsbnOrigin,
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
                            event = ScanEvent.BookResolvedEvent(
                                book = result.book,
                                editionId = result.editionId,
                            ),
                        )
                    }

                    is IsbnLookupResult.UnknownEdition -> {
                        scope.setState {
                            it.copy(
                                isResolving = false,
                                unknownIsbnSheet = UnknownIsbnSheetUiModel(
                                    isbn = result.normalizedIsbn,
                                    scaffold = unknownIsbnSheetScaffoldFor(isAdding = false),
                                ),
                            )
                        }
                    }

                    is IsbnLookupResult.InvalidIsbn -> {
                        when (origin) {
                            IsbnOrigin.Manual -> scope.setState {
                                it.copy(
                                    isResolving = false,
                                    isbnField = it.isbnField.copy(
                                        error = TextFieldError(message = INVALID_ISBN_MESSAGE),
                                    ),
                                )
                            }

                            IsbnOrigin.Camera -> {
                                scope.setState { it.copy(isResolving = false) }

                                scope.sendEvent(event = ScanEvent.InvalidIsbnEvent())
                            }
                        }
                    }
                }
            }
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Failed to resolve scanned ISBN $isbn",
                )

                scope.setState { it.copy(isResolving = false) }

                scope.sendEvent(event = ScanEvent.ResolutionFailedEvent())
            }
    }

    private companion object {
        const val INVALID_ISBN_MESSAGE = "That doesn't look like a valid ISBN."
    }
}
