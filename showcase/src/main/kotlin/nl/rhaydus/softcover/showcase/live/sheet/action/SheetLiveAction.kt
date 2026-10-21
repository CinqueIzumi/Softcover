package nl.rhaydus.softcover.showcase.live.sheet.action

import nl.rhaydus.softcover.showcase.live.sheet.event.SheetLiveEvent
import nl.rhaydus.softcover.showcase.live.sheet.screenmodel.SheetLiveDependencies
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface SheetLiveAction : UiAction<
        SheetLiveDependencies,
        SheetLiveUiState,
        SheetLiveEvent,
        SheetLiveLocalVariables,
        >
