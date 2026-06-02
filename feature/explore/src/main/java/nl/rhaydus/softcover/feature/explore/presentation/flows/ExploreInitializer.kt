package nl.rhaydus.softcover.feature.explore.presentation.flows

import nl.rhaydus.softcover.core.designsystem.presentation.toad.Initializer
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

sealed interface ExploreInitializer : Initializer<
        ExploreScreenUiState,
        ExploreEvent,
        ExploreDependencies,
        ExploreLocalVariables
        >