package nl.rhaydus.softcover.showcase.live.topbar.action

import nl.rhaydus.softcover.showcase.live.topbar.event.TopbarLiveEvent
import nl.rhaydus.softcover.showcase.live.topbar.screenmodel.TopbarLiveDependencies
import nl.rhaydus.softcover.showcase.live.topbar.state.TopbarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.topbar.state.TopbarLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnSearchClearedAction : TopbarLiveAction {
    override suspend fun execute(
        dependencies: TopbarLiveDependencies,
        scope: ActionScope<TopbarLiveUiState, TopbarLiveEvent, TopbarLiveLocalVariables>,
    ) {
        scope.setState {
            it.copy(search = it.search.copy(
                query = "",
                active = false,
                focused = false,
            ),)
        }
    }
}
