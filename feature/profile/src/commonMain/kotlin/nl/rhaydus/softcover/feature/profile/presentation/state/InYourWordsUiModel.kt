package nl.rhaydus.softcover.feature.profile.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.chart.RankedBarsUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.quote.QuoteCardUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel

/**
 * The In your words chapter: the review and quote tiles in [tiles] order, then the book quoted most.
 *
 * @property mostQuoted null when no quote in scope belongs to a library book.
 * @property ownTags null when no book finished in scope carries an own tag.
 */
internal data class InYourWordsUiModel(
    val header: SectionHeaderUiModel.Section,
    val tiles: ImmutableList<StatTileUiModel>,
    val mostQuoted: MostQuotedUiModel?,
    val ownTags: OwnTagsUiModel?,
    val hasContent: Boolean,
    val isLoading: Boolean,
)

internal data class MostQuotedUiModel(
    val title: String,
    val caption: String,
    val card: QuoteCardUiModel.Book,
    val bookId: Int,
)

/** The reader's own tags ranked by finished books in scope; [ranking] rows are tappable. */
internal data class OwnTagsUiModel(
    val title: String,
    val caption: String,
    val ranking: RankedBarsUiModel,
)
