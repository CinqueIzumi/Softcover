package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.UiAction
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

sealed interface ExploreAction : UiAction<
        ExploreDependencies,
        ExploreScreenUiState,
        ExploreEvent,
        ExploreLocalVariables,
        >