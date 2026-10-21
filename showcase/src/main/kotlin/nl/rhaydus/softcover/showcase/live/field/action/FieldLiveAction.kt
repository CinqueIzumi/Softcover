package nl.rhaydus.softcover.showcase.live.field.action

import nl.rhaydus.softcover.showcase.live.field.event.FieldLiveEvent
import nl.rhaydus.softcover.showcase.live.field.screenmodel.FieldLiveDependencies
import nl.rhaydus.softcover.showcase.live.field.state.FieldLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.field.state.FieldLiveUiState
import nl.rhaydus.toad.UiAction

internal sealed interface FieldLiveAction : UiAction<
        FieldLiveDependencies,
        FieldLiveUiState,
        FieldLiveEvent,
        FieldLiveLocalVariables,
        >
