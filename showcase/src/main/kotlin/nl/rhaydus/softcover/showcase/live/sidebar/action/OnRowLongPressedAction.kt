package nl.rhaydus.softcover.showcase.live.sidebar.action

import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.showcase.live.sidebar.event.SidebarLiveEvent
import nl.rhaydus.softcover.showcase.live.sidebar.screenmodel.SidebarLiveDependencies
import nl.rhaydus.softcover.showcase.live.sidebar.state.SidebarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.sidebar.state.SidebarLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnRowLongPressedAction : SidebarLiveAction {
    override suspend fun execute(
        dependencies: SidebarLiveDependencies,
        scope: ActionScope<SidebarLiveUiState, SidebarLiveEvent, SidebarLiveLocalVariables>,
    ) {
        SnackBarManager.showSnackbar(
            title = "A long press opens the row's own menu, in the real app.",
        )
    }
}
