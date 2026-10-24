package nl.rhaydus.softcover.feature.profile.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

internal data class ReadingSummaryUiModel(
    val header: SectionHeaderUiModel,
    val stats: ImmutableList<ReadingSummaryStatUiModel>,
    val linkLabel: String,
)
