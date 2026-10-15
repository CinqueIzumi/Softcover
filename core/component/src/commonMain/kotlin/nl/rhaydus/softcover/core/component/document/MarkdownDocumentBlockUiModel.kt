package nl.rhaydus.softcover.core.component.document

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

/** One block of a [MarkdownDocumentUiModel], in reading order. */
@Immutable
sealed interface MarkdownDocumentBlockUiModel {
    /** A heading shaped as an eyebrow and a headline, drawn through the Section header register. */
    data class SectionHeading(val header: SectionHeaderUiModel.Section) : MarkdownDocumentBlockUiModel

    data class PlainHeading(val text: String) : MarkdownDocumentBlockUiModel

    /** A run of consecutive list entries, drawn as one hanging-indent list. */
    data class ListGroup(val items: ImmutableList<MarkdownDocumentListItemUiModel>) : MarkdownDocumentBlockUiModel

    data class Paragraph(val spans: ImmutableList<MarkdownDocumentSpanUiModel>) : MarkdownDocumentBlockUiModel

    data class BlockQuote(val spans: ImmutableList<MarkdownDocumentSpanUiModel>) : MarkdownDocumentBlockUiModel

    data object ThematicBreak : MarkdownDocumentBlockUiModel
}
