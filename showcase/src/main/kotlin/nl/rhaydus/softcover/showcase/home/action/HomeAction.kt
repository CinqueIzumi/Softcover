package nl.rhaydus.softcover.showcase.home.action

import nl.rhaydus.softcover.showcase.home.event.HomeEvent
import nl.rhaydus.softcover.showcase.home.screenmodel.HomeDependencies
import nl.rhaydus.softcover.showcase.home.state.HomeLocalVariables
import nl.rhaydus.softcover.showcase.home.state.HomeUiState
import nl.rhaydus.toad.UiAction

internal sealed interface HomeAction : UiAction<
        HomeDependencies,
        HomeUiState,
        HomeEvent,
        HomeLocalVariables,
        >
