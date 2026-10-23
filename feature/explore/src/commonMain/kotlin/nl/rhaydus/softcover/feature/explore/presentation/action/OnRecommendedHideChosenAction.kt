package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.softcover.feature.explore.presentation.state.PendingRecommendedHide
import nl.rhaydus.toad.ActionScope

internal class OnRecommendedHideChosenAction(
    val bookId: Int,
) : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        val pending = PendingRecommendedHide(bookId = bookId)

        scope.setState {
            it.copy(
                recommendedMenu = it.recommendedMenu.requestClose(pending = pending),
                recommendedMenuBookId = null,
            )
        }
    }
}
