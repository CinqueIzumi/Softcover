package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.lists.domain.model.HiddenRecommendation
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope

internal class OnRestoreRecommendedBookAction(
    val book: HiddenRecommendation,
) : HiddenSuggestionsAction {
    override suspend fun execute(
        dependencies: HiddenSuggestionsDependencies,
        scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>,
    ) {
        dependencies.restoreRecommendedBookUseCase(bookId = book.bookId)
            .onFailure {
                AppLog.e(
                    it,
                    "Failed to restore recommended book ${book.bookId}",
                )
            }
            .onSuccess {
                SnackBarManager.showSnackBar(
                    title = "\"${book.title ?: "Book"}\" is back in your suggestions",
                    actionLabel = "Undo",
                    onActionClick = {
                        dependencies.launch {
                            dependencies.hideRecommendedBookUseCase(book = book)
                                .onFailure {
                                    AppLog.e(
                                        it,
                                        "Failed to re-hide recommended book ${book.bookId} after restore undo",
                                    )
                                }
                        }
                    },
                )
            }
    }
}
