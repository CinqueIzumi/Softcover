package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.designsystem.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import timber.log.Timber

class OnDismissContinueSeriesBookAction(
    val bookId: Int,
    val bookTitle: String,
) : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        dependencies.dismissContinueSeriesBookUseCase(bookId = bookId)
            .onFailure { Timber.e(
                it,
                "Failed to dismiss book $bookId from continue-series",
            ) }
            .onSuccess {
                SnackBarManager.showSnackBar(
                    title = "\"$bookTitle\" won't be suggested again",
                    actionLabel = "Undo",
                    onActionClick = {
                        dependencies.launch {
                            dependencies.undoContinueSeriesBookDismissalUseCase(bookId = bookId)
                                .onFailure { Timber.e(
                                    it,
                                    "Failed to undo book dismissal",
                                ) }
                        }
                    },
                )
            }
    }
}
