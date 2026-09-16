package nl.rhaydus.softcover.feature.settings.presentation.state

import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument
import nl.rhaydus.toad.UiState

internal data class RoadmapUiState(
    val document: RoadmapDocument? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val roadmapError: String? = null,

    /**
     * The "Last updated" footer's date text, mapped by `RoadmapDocumentCollector` (R9) off
     * [document]'s fetch time. Null exactly when the footer does not render — [document] has no
     * fetch time yet.
     */
    val lastUpdatedText: String? = null,
) : UiState
