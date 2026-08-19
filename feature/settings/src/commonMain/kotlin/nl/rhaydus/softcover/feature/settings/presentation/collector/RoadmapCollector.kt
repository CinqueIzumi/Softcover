package nl.rhaydus.softcover.feature.settings.presentation.collector

import nl.rhaydus.softcover.feature.settings.presentation.event.RoadmapEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.RoadmapDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.RoadmapUiState
import nl.rhaydus.toad.Collector

internal sealed interface RoadmapCollector : Collector<
        RoadmapUiState,
        RoadmapEvent,
        RoadmapDependencies,
        RoadmapLocalVariables,
        >
