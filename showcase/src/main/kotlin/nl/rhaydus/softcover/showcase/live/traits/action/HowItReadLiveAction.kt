package nl.rhaydus.softcover.showcase.live.traits.action

import nl.rhaydus.softcover.showcase.live.traits.event.HowItReadLiveEvent
import nl.rhaydus.softcover.showcase.live.traits.screenmodel.HowItReadLiveDependencies
import nl.rhaydus.softcover.showcase.live.traits.state.HowItReadLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.traits.state.HowItReadLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface HowItReadLiveAction : UiAction<
        HowItReadLiveDependencies,
        HowItReadLiveUiState,
        HowItReadLiveEvent,
        HowItReadLiveLocalVariables,
        >
