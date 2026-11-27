package nl.rhaydus.softcover.feature.profile.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel

/**
 * The In your words chapter: the review and quote tiles in [tiles] order, then the book quoted most.
 *
 * @property tiles empty when nothing was written in scope, where the header's description says what would
 * show here; the loading stand-in is not empty.
 * @property mostQuoted null when no quote in scope belongs to a library book.
 * @property ownTags null when no book finished in scope carries an own tag.
 */
internal data class InYourWordsUiModel(
    val header: SectionHeaderUiModel.Section,
    val tiles: ImmutableList<StatTileUiModel>,
    val mostQuoted: MostQuotedUiModel?,
    val ownTags: OwnTagsUiModel?,
    val isLoading: Boolean,
)
