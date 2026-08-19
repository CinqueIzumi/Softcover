package nl.rhaydus.softcover.feature.settings.presentation.state

import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument
import nl.rhaydus.toad.UiState

internal data class RoadmapUiState(
    val document: RoadmapDocument? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val roadmapError: String? = null,
) : UiState
