package nl.rhaydus.softcover.core.component.share

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel

/**
 * The plain editorial book card — a share of the book itself, not the reader's relationship to it (see
 * [ReadingUpdateShareCardUiModel] for that). Every field but [cover], [title] and [author] is nullable so
 * the card renders whatever the caller actually knows about the book, omitting the rest. [statsLine] is
 * the ready-made "★ 4.2 · Your 9/10 · 1962 · 214 pages" line. [tags] are the ones the sharer chose to
 * put on the card, already in display order.
 */
data class BookShareCardUiModel(
    val cover: CoverUiModel,
    val title: String,
    val author: String,
    val statsLine: String?,
    val description: String?,
    val tags: ImmutableList<ChipUiModel> = persistentListOf(),
    val format: ShareCardFormat = ShareCardFormat.Story,
) : ShareCardUiModel
