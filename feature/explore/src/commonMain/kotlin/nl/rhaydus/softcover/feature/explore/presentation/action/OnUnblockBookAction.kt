package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope

internal class OnUnblockBookAction(
    val bookId: Int,
    val title: String?,
    val coverUrl: String?,
    val authorText: String?,
    val seriesName: String?,
) : HiddenSuggestionsAction {
    override suspend fun execute(
        dependencies: HiddenSuggestionsDependencies,
        scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>,
    ) {
        dependencies.undoContinueSeriesBookDismissalUseCase(bookId = bookId)
            .onFailure {
                AppLog.e(
                    it,
                    "Failed to unblock book $bookId",
                )
            }
            .onSuccess {
                SnackBarManager.showSnackBar(
                    title = "\"${title ?: "Book"}\" unblocked",
                    actionLabel = "Undo",
                    onActionClick = {
                        dependencies.launch {
                            dependencies.dismissContinueSeriesBookUseCase(
                                bookId = bookId,
                                title = title,
                                coverUrl = coverUrl,
                                authorText = authorText,
                                seriesName = seriesName,
                            ).onFailure {
                                AppLog.e(
                                    it,
                                    "Failed to re-hide book $bookId after unblock undo",
                                )
                            }
                        }
                    },
                )
            }
    }
}
