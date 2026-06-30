package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal class OnRemoveAllSearchQueriesClickedAction : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        dependencies.removeAllSearchQueriesUseCase().onFailure {
            AppLog.e("$it")
        }
    }
}
