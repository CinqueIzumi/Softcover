package nl.rhaydus.softcover.core.component.document

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/** One list entry: its [marker] (`"•"` or `"3."`) and the text hanging beside it. */
@Immutable
data class MarkdownDocumentListItemUiModel(
    val marker: String,
    val spans: ImmutableList<MarkdownDocumentSpanUiModel>,
)
