package nl.rhaydus.softcover.showcase.live.sidebar.action

import nl.rhaydus.softcover.showcase.live.sidebar.event.SidebarLiveEvent
import nl.rhaydus.softcover.showcase.live.sidebar.screenmodel.SidebarLiveDependencies
import nl.rhaydus.softcover.showcase.live.sidebar.state.SidebarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.sidebar.state.SidebarLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface SidebarLiveAction : UiAction<
        SidebarLiveDependencies,
        SidebarLiveUiState,
        SidebarLiveEvent,
        SidebarLiveLocalVariables,
        >
