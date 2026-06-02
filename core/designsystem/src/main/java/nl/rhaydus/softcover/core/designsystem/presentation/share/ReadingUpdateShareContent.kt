package nl.rhaydus.softcover.core.designsystem.presentation.share

import nl.rhaydus.softcover.core.domain.model.ReviewDocument

/**
 * A personalised "reading update" card — the user sharing *their* relationship with a book rather
 * than the book itself. Finished books show the reader's rating (as stars) and their review;
 * in-progress books show their reading progress instead.
 *
 * The review travels as a structured [ReviewDocument] (not a flattened string) so the card can mask
 * inline spoiler spans the same way the book-detail screen does — see the `ReviewDocumentText`
 * render in [ShareCard]. The remaining display values are pre-computed by the caller so [ShareCard]
 * stays a dumb renderer.
 */
data class ReadingUpdateShareContent(
    val username: String,
    val avatarUrl: String?,
    val coverUrl: String?,
    val title: String,
    val author: String,
    val kind: ReadingUpdateKind,
    val ratingStars: Double?,
    val review: ReviewDocument?,
    val progressLabel: String?,
    val tags: List<String> = emptyList(),
) : ShareContent
