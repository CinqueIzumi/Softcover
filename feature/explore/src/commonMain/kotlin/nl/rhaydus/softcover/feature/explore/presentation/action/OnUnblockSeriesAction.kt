package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope

internal class OnUnblockSeriesAction(
    val seriesId: Int,
    val seriesName: String?,
    val coverUrl: String?,
) : HiddenSuggestionsAction {
    override suspend fun execute(
        dependencies: HiddenSuggestionsDependencies,
        scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>,
    ) {
        dependencies.undoContinueSeriesDismissalUseCase(seriesId = seriesId)
            .onFailure {
                AppLog.e(
                    it,
                    "Failed to unblock series $seriesId",
                )
            }
            .onSuccess {
                SnackBarManager.showSnackBar(
                    title = "\"${seriesName ?: "Series"}\" unblocked",
                    actionLabel = "Undo",
                    onActionClick = {
                        dependencies.launch {
                            dependencies.dismissContinueSeriesUseCase(
                                seriesId = seriesId,
                                seriesName = seriesName,
                                coverUrl = coverUrl,
                            ).onFailure {
                                AppLog.e(
                                    it,
                                    "Failed to re-hide series $seriesId after unblock undo",
                                )
                            }
                        }
                    },
                )
            }
    }
}
