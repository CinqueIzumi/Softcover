package nl.rhaydus.softcover.feature.book_detail.presentation.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.quote.QuoteCardUiModel

/**
 * The Yours lens "Your quotes" section. [emptyMessage] is non-null exactly while [quotes] is empty, and
 * [addChip] opens the quote sheet.
 */
@Immutable
internal data class QuotesSectionUiModel(
    val header: SectionHeaderUiModel,
    val quotes: ImmutableList<QuoteCardUiModel>,
    val emptyMessage: String?,
    val addChip: ChipUiModel,
)
