package nl.rhaydus.softcover.feature.explore.presentation.collector

import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.Collector

internal sealed interface ExploreCollector : Collector<
        ExploreScreenUiState,
        ExploreEvent,
        ExploreDependencies,
        ExploreLocalVariables
        >
