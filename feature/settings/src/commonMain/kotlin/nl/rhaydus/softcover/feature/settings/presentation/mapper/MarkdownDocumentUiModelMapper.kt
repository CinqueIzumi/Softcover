package nl.rhaydus.softcover.feature.settings.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentBlockUiModel
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentListItemUiModel
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentSpanUiModel
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownBlock
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownSpan

private const val SECTION_HEADING_LEVEL = 2

/**
 * Shapes a document's flat block list for the `MarkdownDocument` component:
 * every consecutive run of list items becomes one list group, since the domain model emits items as flat
 * top-level blocks.
 */
internal fun List<MarkdownBlock>.toMarkdownDocumentUiModel(): MarkdownDocumentUiModel {
    val blocks = mutableListOf<MarkdownDocumentBlockUiModel>()
    var index = 0

    while (index < size) {
        when (val block = this[index]) {
            is MarkdownBlock.Heading -> {
                blocks += block.toBlockUiModel()
                index += 1
            }

            is MarkdownBlock.BulletListItem, is MarkdownBlock.OrderedListItem -> {
                val runStart = index

                while (index < size && this[index].isListItem()) {
                    index += 1
                }

                blocks += MarkdownDocumentBlockUiModel.ListGroup(
                    items = subList(
                        runStart,
                        index,
                    ).mapNotNull { it.toListItemUiModel() }.toImmutableList(),
                )
            }

            is MarkdownBlock.Paragraph -> {
                blocks += MarkdownDocumentBlockUiModel.Paragraph(spans = block.spans.toSpanUiModels())
                index += 1
            }

            is MarkdownBlock.BlockQuote -> {
                blocks += MarkdownDocumentBlockUiModel.BlockQuote(spans = block.spans.toSpanUiModels())
                index += 1
            }

            MarkdownBlock.ThematicBreak -> {
                blocks += MarkdownDocumentBlockUiModel.ThematicBreak
                index += 1
            }
        }
    }

    return MarkdownDocumentUiModel(blocks = blocks.toImmutableList())
}

private fun MarkdownBlock.isListItem(): Boolean =
    this is MarkdownBlock.BulletListItem || this is MarkdownBlock.OrderedListItem

private fun MarkdownBlock.toListItemUiModel(): MarkdownDocumentListItemUiModel? = when (this) {
    is MarkdownBlock.BulletListItem -> MarkdownDocumentListItemUiModel(
        marker = "•",
        spans = spans.toSpanUiModels(),
    )

    is MarkdownBlock.OrderedListItem -> MarkdownDocumentListItemUiModel(
        marker = "$index.",
        spans = spans.toSpanUiModels(),
    )

    else -> null
}

/**
 * A level-2 heading shaped `"3.2.0: Widgets, and a lot of small things"` splits on the first `": "` into
 * an eyebrow (the version) + headline (the title); any other heading (`"Under consideration"`, a level-3
 * `"Fixed: the crash"`) degrades to [MarkdownDocumentBlockUiModel.PlainHeading].
 */
private fun MarkdownBlock.Heading.toBlockUiModel(): MarkdownDocumentBlockUiModel {
    val plainText = spans.joinToString(separator = "") { it.text }
    val separatorIndex = plainText.indexOf(": ")

    return if (level == SECTION_HEADING_LEVEL && separatorIndex > 0 && separatorIndex < plainText.length - 2) {
        MarkdownDocumentBlockUiModel.SectionHeading(
            header = SectionHeaderUiModel.Section(
                eyebrow = plainText.substring(
                    startIndex = 0,
                    endIndex = separatorIndex,
                ),
                headline = plainText.substring(startIndex = separatorIndex + 2),
            ),
        )
    } else {
        MarkdownDocumentBlockUiModel.PlainHeading(text = plainText)
    }
}

private fun ImmutableList<MarkdownSpan>.toSpanUiModels(): ImmutableList<MarkdownDocumentSpanUiModel> = map { span ->
    MarkdownDocumentSpanUiModel(
        text = span.text,
        bold = span.bold,
        italic = span.italic,
        code = span.code,
        url = span.url,
    )
}.toImmutableList()
