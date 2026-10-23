package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.lists.domain.model.HiddenRecommendation
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal class OnHideRecommendedBookAction(
    val book: HiddenRecommendation,
) : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        dependencies.hideRecommendedBookUseCase(book = book)
            .onFailure {
                AppLog.e(
                    it,
                    "Failed to hide book ${book.bookId} from recommendations",
                )
            }
            .onSuccess {
                SnackBarManager.showSnackBar(
                    title = "\"${book.title ?: "Book"}\" won't be suggested again",
                    actionLabel = "Undo",
                    onActionClick = {
                        dependencies.launch {
                            dependencies.restoreRecommendedBookUseCase(bookId = book.bookId)
                                .onFailure {
                                    AppLog.e(
                                        it,
                                        "Failed to undo hiding book ${book.bookId} from recommendations",
                                    )
                                }
                        }
                    },
                )
            }
    }
}
