package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel

/** One side of a two-cell comparison; the [isLead] cell is the one the pair reads towards. */
internal data class VersusCellUiModel(
    val eyebrow: String,
    val stat: StatNumberUiModel,
    val caption: String,
    val isLead: Boolean,
)
