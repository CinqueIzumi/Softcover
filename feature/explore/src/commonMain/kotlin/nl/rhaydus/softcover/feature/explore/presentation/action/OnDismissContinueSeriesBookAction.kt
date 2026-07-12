package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal class OnDismissContinueSeriesBookAction(
    val book: DismissedSeriesBook,
) : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        dependencies.dismissContinueSeriesBookUseCase(book = book)
            .onFailure {
                AppLog.e(
                    it,
                    "Failed to dismiss book ${book.bookId} from continue-series",
                )
            }
            .onSuccess {
                SnackBarManager.showSnackBar(
                    title = "\"${book.title ?: "Book"}\" won't be suggested again",
                    actionLabel = "Undo",
                    onActionClick = {
                        dependencies.launch {
                            dependencies.undoContinueSeriesBookDismissalUseCase(bookId = book.bookId)
                                .onFailure {
                                    AppLog.e(
                                        it,
                                        "Failed to undo book dismissal",
                                    )
                                }
                        }
                    },
                )
            }
    }
}
