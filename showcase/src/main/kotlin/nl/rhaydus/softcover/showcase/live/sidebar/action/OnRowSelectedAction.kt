package nl.rhaydus.softcover.showcase.live.sidebar.action

import nl.rhaydus.softcover.showcase.live.sidebar.event.SidebarLiveEvent
import nl.rhaydus.softcover.showcase.live.sidebar.mapper.sidebarModel
import nl.rhaydus.softcover.showcase.live.sidebar.screenmodel.SidebarLiveDependencies
import nl.rhaydus.softcover.showcase.live.sidebar.state.SidebarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.sidebar.state.SidebarLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnRowSelectedAction(val key: String) : SidebarLiveAction {
    override suspend fun execute(
        dependencies: SidebarLiveDependencies,
        scope: ActionScope<SidebarLiveUiState, SidebarLiveEvent, SidebarLiveLocalVariables>,
    ) {
        scope.setState {
            it.copy(sidebar = sidebarModel(selected = key))
        }
    }
}
