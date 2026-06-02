package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.designsystem.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import timber.log.Timber

class OnDismissContinueSeriesAction(
    val seriesId: Int,
    val seriesName: String,
) : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        dependencies.dismissContinueSeriesUseCase(seriesId = seriesId)
            .onFailure { Timber.e(
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
                                .onFailure { Timber.e(
                                    it,
                                    "Failed to undo series dismissal",
                                ) }
                        }
                    },
                )
            }
    }
}
