package nl.rhaydus.softcover.showcase.live.chip.action

import nl.rhaydus.softcover.showcase.live.chip.event.ChipLiveEvent
import nl.rhaydus.softcover.showcase.live.chip.screenmodel.ChipLiveDependencies
import nl.rhaydus.softcover.showcase.live.chip.state.ChipLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.chip.state.ChipLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface ChipLiveAction : UiAction<
        ChipLiveDependencies,
        ChipLiveUiState,
        ChipLiveEvent,
        ChipLiveLocalVariables,
        >
