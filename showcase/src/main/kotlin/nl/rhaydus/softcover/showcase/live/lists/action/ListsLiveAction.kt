package nl.rhaydus.softcover.showcase.live.lists.action

import nl.rhaydus.softcover.showcase.live.lists.event.ListsLiveEvent
import nl.rhaydus.softcover.showcase.live.lists.screenmodel.ListsLiveDependencies
import nl.rhaydus.softcover.showcase.live.lists.state.ListsLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.lists.state.ListsLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface ListsLiveAction : UiAction<
        ListsLiveDependencies,
        ListsLiveUiState,
        ListsLiveEvent,
        ListsLiveLocalVariables,
        >
