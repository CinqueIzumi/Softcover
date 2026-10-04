package nl.rhaydus.softcover.core.component.share

/**
 * The plain editorial book card — a share of the book itself, not the reader's relationship to it (see
 * [ReadingUpdateShareCardUiModel] for that). Every field but [title] and [author] is nullable so the
 * card renders whatever the caller actually knows about the book, omitting the rest. [statsLine] is
 * the ready-made "★ 4.2 · Your 9/10 · 1962 · 214 pages" line.
 */
data class BookShareCardUiModel(
    val coverUrl: String?,
    val title: String,
    val author: String,
    val statsLine: String?,
    val description: String?,
) : ShareCardUiModel
