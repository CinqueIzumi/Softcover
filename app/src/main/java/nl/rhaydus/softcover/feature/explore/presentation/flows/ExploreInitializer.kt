package nl.rhaydus.softcover.feature.explore.presentation.flows

import nl.rhaydus.softcover.core.presentation.toad.Initializer
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies

sealed interface ExploreInitializer : Initializer<
        ExploreScreenUiState,
        ExploreEvent,
        ExploreDependencies,
        ExploreLocalVariables
        >