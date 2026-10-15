package nl.rhaydus.softcover.feature.settings.domain.model

import kotlinx.collections.immutable.ImmutableList

/**
 * One structural unit of a parsed [PublishedDocument], in source order. List items are flat, top-level
 * blocks rather than grouped into a list container - the markdown mapper emits them one per line, and
 * grouping consecutive [BulletListItem] / [OrderedListItem] runs is left to the presentation mapper,
 * which already walks the block sequence.
 */
sealed interface MarkdownBlock {
    data class Heading(
        val level: Int,
        val spans: ImmutableList<MarkdownSpan>,
    ) : MarkdownBlock

    data class Paragraph(val spans: ImmutableList<MarkdownSpan>) : MarkdownBlock

    data class BulletListItem(val spans: ImmutableList<MarkdownSpan>) : MarkdownBlock

    data class OrderedListItem(
        val index: Int,
        val spans: ImmutableList<MarkdownSpan>,
    ) : MarkdownBlock

    data class BlockQuote(val spans: ImmutableList<MarkdownSpan>) : MarkdownBlock

    data object ThematicBreak : MarkdownBlock
}
