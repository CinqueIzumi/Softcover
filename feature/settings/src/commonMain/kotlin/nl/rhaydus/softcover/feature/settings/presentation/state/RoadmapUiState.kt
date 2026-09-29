package nl.rhaydus.softcover.feature.settings.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.state.SkeletonUiModel
import nl.rhaydus.softcover.core.component.state.SkeletonVariant
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument
import nl.rhaydus.softcover.feature.settings.presentation.model.RoadmapRenderUnit
import nl.rhaydus.toad.UiState

internal data class RoadmapUiState(
    val document: RoadmapDocument? = null,

    val renderUnits: ImmutableList<RoadmapRenderUnit> = persistentListOf(),

    /** Desktop only — mobile's pushed Roadmap screen renders its own top bar instead. */
    val masthead: PageMastheadUiModel = PageMastheadUiModel(
        eyebrow = "Roadmap",
        title = "Roadmap",
        subtitle = "What we're building next, and roughly when.",
    ),
    val skeleton: SkeletonUiModel = SkeletonUiModel(SkeletonVariant.RoadmapEntry),
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
