package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.UiAction

internal sealed interface StatsAtlasAction : UiAction<
        StatsAtlasDependencies,
        StatsAtlasUiState,
        StatsAtlasEvent,
        StatsAtlasLocalVariables,
        >
