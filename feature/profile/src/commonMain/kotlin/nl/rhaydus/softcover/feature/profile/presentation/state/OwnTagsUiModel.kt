package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.chart.RankedBarsUiModel

/** The reader's own tags ranked by finished books in scope; [ranking] rows are tappable. */
internal data class OwnTagsUiModel(
    val title: String,
    val caption: String,
    val ranking: RankedBarsUiModel,
)
