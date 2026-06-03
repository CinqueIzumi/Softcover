package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.designsystem.presentation.util.SnackBarManager
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

internal class OnDismissContinueSeriesAction(
    val seriesId: Int,
    val seriesName: String,
) : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        dependencies.dismissContinueSeriesUseCase(seriesId = seriesId)
            .onFailure { AppLog.e(
                it,
                "Failed to dismiss series $seriesId",
            ) }
            .onSuccess {
                SnackBarManager.showSnackBar(
                    title = "\"$seriesName\" won't be suggested again",
                    actionLabel = "Undo",
                    onActionClick = {
                        dependencies.launch {
                            dependencies.undoContinueSeriesDismissalUseCase(seriesId = seriesId)
                                .onFailure { AppLog.e(
                                    it,
                                    "Failed to undo series dismissal",
                                ) }
                        }
                    },
                )
            }
    }
}
