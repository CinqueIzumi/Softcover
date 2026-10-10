package nl.rhaydus.softcover.feature.settings.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.header.PageMastheadSubtitle
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument
import nl.rhaydus.softcover.feature.settings.presentation.mapper.ROADMAP_STAND_IN_UNITS
import nl.rhaydus.softcover.feature.settings.presentation.model.RoadmapRenderUnit
import nl.rhaydus.toad.UiState

private const val ROADMAP_TITLE = "Roadmap"

internal data class RoadmapUiState(
    val document: RoadmapDocument? = null,

    // Masked stand-ins until the first document has loaded.
    val renderUnits: ImmutableList<RoadmapRenderUnit> = ROADMAP_STAND_IN_UNITS,

    /** Desktop only — mobile's pushed Roadmap screen renders its own top bar instead. */
    val masthead: PageMastheadUiModel = PageMastheadUiModel(
        eyebrow = "Roadmap",
        title = "Roadmap",
        subtitle = PageMastheadSubtitle.Text("What we're building next, and roughly when."),
    ),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val roadmapError: String? = null,

    /**
     * The "Last updated" footer's date text. `null` exactly when the footer does not render — [document]
     * has no fetch time yet.
     */
    val lastUpdatedText: String? = null,
    val topBar: TopBarUiModel = TopBarUiModel(
        title = ROADMAP_TITLE,
    ),
) : UiState
