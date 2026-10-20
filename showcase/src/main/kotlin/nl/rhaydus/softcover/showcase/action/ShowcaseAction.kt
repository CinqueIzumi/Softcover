package nl.rhaydus.softcover.showcase.action

import nl.rhaydus.softcover.showcase.event.ShowcaseEvent
import nl.rhaydus.softcover.showcase.screenmodel.ShowcaseDependencies
import nl.rhaydus.softcover.showcase.state.ShowcaseLocalVariables
import nl.rhaydus.softcover.showcase.state.ShowcaseUiState
import nl.rhaydus.toad.UiAction

internal sealed interface ShowcaseAction : UiAction<
        ShowcaseDependencies,
        ShowcaseUiState,
        ShowcaseEvent,
        ShowcaseLocalVariables,
        >
