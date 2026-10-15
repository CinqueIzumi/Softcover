package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.component.share.ShareCardFormat

internal enum class ShareCardSize(
    val label: String,
    val format: ShareCardFormat,
) {
    STORY(
        label = "Story",
        format = ShareCardFormat.Story,
    ),
    FULL_TEXT(
        label = "Full text",
        format = ShareCardFormat.FullText,
    ),
}
