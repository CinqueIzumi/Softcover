package nl.rhaydus.softcover.showcase.live.topbar.action

import nl.rhaydus.softcover.showcase.live.topbar.event.TopbarLiveEvent
import nl.rhaydus.softcover.showcase.live.topbar.screenmodel.TopbarLiveDependencies
import nl.rhaydus.softcover.showcase.live.topbar.state.TopbarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.topbar.state.TopbarLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface TopbarLiveAction : UiAction<
        TopbarLiveDependencies,
        TopbarLiveUiState,
        TopbarLiveEvent,
        TopbarLiveLocalVariables,
        >
