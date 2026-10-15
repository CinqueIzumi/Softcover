package nl.rhaydus.softcover.feature.book_detail.presentation.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/** The Yours lens "Your reads" section; [summary] is null while no read is finished. */
@Immutable
internal data class YourReadsUiModel(
    val header: SectionHeaderUiModel,
    val summary: String?,
    val rows: ImmutableList<ReadThroughRowUiModel>,
)
