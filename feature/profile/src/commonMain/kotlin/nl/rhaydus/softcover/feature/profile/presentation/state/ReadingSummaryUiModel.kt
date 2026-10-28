package nl.rhaydus.softcover.feature.profile.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.chart.RecentDaysGridUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

internal data class ReadingSummaryUiModel(
    val header: SectionHeaderUiModel,
    val stats: ImmutableList<ReadingSummaryStatUiModel>,
    val recentDays: RecentDaysGridUiModel,
    val recentDaysCaption: String,
    val linkLabel: String,
)
