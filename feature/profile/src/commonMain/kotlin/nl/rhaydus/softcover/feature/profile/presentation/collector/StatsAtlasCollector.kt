package nl.rhaydus.softcover.feature.profile.presentation.collector

import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.Collector

internal sealed interface StatsAtlasCollector : Collector<
        StatsAtlasUiState,
        StatsAtlasEvent,
        StatsAtlasDependencies,
        StatsAtlasLocalVariables,
        >
