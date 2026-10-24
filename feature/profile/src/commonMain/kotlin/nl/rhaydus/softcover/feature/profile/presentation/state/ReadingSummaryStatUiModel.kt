package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel

internal data class ReadingSummaryStatUiModel(
    val stat: StatNumberUiModel,
    val caption: String,
)
