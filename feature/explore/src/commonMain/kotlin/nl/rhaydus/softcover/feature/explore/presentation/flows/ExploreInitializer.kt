package nl.rhaydus.softcover.feature.explore.presentation.flows

import nl.rhaydus.softcover.core.designsystem.presentation.toad.Collector
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

internal sealed interface ExploreInitializer : Collector<
        ExploreScreenUiState,
        ExploreEvent,
        ExploreDependencies,
        ExploreLocalVariables
        >
