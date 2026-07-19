package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope

internal class OnUnblockBookAction(
    val book: DismissedSeriesBook,
) : HiddenSuggestionsAction {
    override suspend fun execute(
        dependencies: HiddenSuggestionsDependencies,
        scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>,
    ) {
        dependencies.undoContinueSeriesBookDismissalUseCase(bookId = book.bookId)
            .onFailure {
                AppLog.e(
                    it,
                    "Failed to unblock book ${book.bookId}",
                )
            }
            .onSuccess {
                SnackBarManager.showSnackBar(
                    title = "\"${book.title ?: "Book"}\" is back in your suggestions",
                    actionLabel = "Undo",
                    onActionClick = {
                        dependencies.launch {
                            // Re-hiding replays the whole row, cursor included: dropping the series
                            // position here would leave the series stuck on this book again.
                            dependencies.dismissContinueSeriesBookUseCase(book = book)
                                .onFailure {
                                    AppLog.e(
                                        it,
                                        "Failed to re-hide book ${book.bookId} after unblock undo",
                                    )
                                }
                        }
                    },
                )
            }
    }
}
