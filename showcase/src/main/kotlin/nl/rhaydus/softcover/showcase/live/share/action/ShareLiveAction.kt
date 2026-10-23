package nl.rhaydus.softcover.showcase.live.share.action

import nl.rhaydus.softcover.showcase.live.share.event.ShareLiveEvent
import nl.rhaydus.softcover.showcase.live.share.screenmodel.ShareLiveDependencies
import nl.rhaydus.softcover.showcase.live.share.state.ShareLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.share.state.ShareLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface ShareLiveAction : UiAction<
        ShareLiveDependencies,
        ShareLiveUiState,
        ShareLiveEvent,
        ShareLiveLocalVariables,
        >
