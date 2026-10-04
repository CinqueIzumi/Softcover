package nl.rhaydus.softcover.core.component.share

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel

/**
 * A personalised "reading update" card — the user sharing *their* relationship with a book rather
 * than the book itself. Neither case carries any recency wording since a reader may share long after
 * the fact: [Finished] shows the reader's rating (as stars) and their review, [Reading] shows its
 * [Reading.progressLabel] instead.
 *
 * The remaining display values are pre-computed by the caller so [ShareCard] stays a dumb renderer.
 */
sealed interface ReadingUpdateShareCardUiModel : ShareCardUiModel {
    val username: String
    val avatarUrl: String?
    val coverUrl: String?
    val title: String
    val author: String

    /**
     * The reader's own tags on this book. **The caller excludes any spoiler-flagged tag** before
     * building this model — a share image is public, so a tag the reader hid from other readers must
     * not leak onto it.
     */
    val tags: ImmutableList<String>

    /**
     * [ratingStars] is on the 0–5 star scale. [review] travels as a structured [RichTextUiModel] so
     * the card can mask inline spoiler spans the same way the book-detail screen does.
     */
    data class Finished(
        override val username: String,
        override val avatarUrl: String?,
        override val coverUrl: String?,
        override val title: String,
        override val author: String,
        override val tags: ImmutableList<String>,
        val ratingStars: Double?,
        val review: RichTextUiModel?,
    ) : ReadingUpdateShareCardUiModel

    data class Reading(
        override val username: String,
        override val avatarUrl: String?,
        override val coverUrl: String?,
        override val title: String,
        override val author: String,
        override val tags: ImmutableList<String>,
        val progressLabel: String?,
    ) : ReadingUpdateShareCardUiModel
}
