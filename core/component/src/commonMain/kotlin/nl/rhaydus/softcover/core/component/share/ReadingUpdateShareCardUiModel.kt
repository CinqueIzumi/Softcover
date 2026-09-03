package nl.rhaydus.softcover.core.component.share

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel

/**
 * A personalised "reading update" card — the user sharing *their* relationship with a book rather
 * than the book itself. Finished books show the reader's rating (as stars) and their review;
 * in-progress books show their reading progress instead.
 *
 * The review travels as a structured [RichTextUiModel] (not a flattened string) so the card can mask
 * inline spoiler spans the same way the book-detail screen does — see the `RichText`
 * render in [ShareCard]. The remaining display values are pre-computed by the caller so [ShareCard]
 * stays a dumb renderer.
 */
data class ReadingUpdateShareCardUiModel(
    val username: String,
    val avatarUrl: String?,
    val coverUrl: String?,
    val title: String,
    val author: String,
    val kind: ReadingUpdateKind,
    val ratingStars: Double?,
    val review: RichTextUiModel?,
    val progressLabel: String?,
    val tags: ImmutableList<String> = persistentListOf(),
) : ShareCardUiModel
