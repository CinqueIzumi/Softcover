package nl.rhaydus.softcover.core.component.document

import androidx.compose.runtime.Immutable

/**
 * One inline run of a block's text. Marks are independent flags rather than a nested tree, so
 * "bold and italic" is one run with both set. A run with a [url] renders as a link.
 */
@Immutable
data class MarkdownDocumentSpanUiModel(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val code: Boolean = false,
    val url: String? = null,
)
