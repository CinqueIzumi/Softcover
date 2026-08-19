package nl.rhaydus.softcover.feature.settings.domain.model

/**
 * One structural unit of a parsed [RoadmapDocument], in source order. List items are flat, top-level
 * blocks rather than grouped into a list container - [RoadmapMarkdownMapper][nl.rhaydus.softcover.feature.settings.data.mapper]
 * emits them one per line, and grouping consecutive [BulletListItem] / [OrderedListItem] runs into a
 * rendered `<ul>`/`<ol>` is left to the renderer, which already walks the block sequence.
 */
sealed interface RoadmapBlock {
    data class Heading(
        val level: Int,
        val spans: List<RoadmapSpan>,
    ) : RoadmapBlock

    data class Paragraph(val spans: List<RoadmapSpan>) : RoadmapBlock

    data class BulletListItem(val spans: List<RoadmapSpan>) : RoadmapBlock

    data class OrderedListItem(
        val index: Int,
        val spans: List<RoadmapSpan>,
    ) : RoadmapBlock

    data class BlockQuote(val spans: List<RoadmapSpan>) : RoadmapBlock

    data object ThematicBreak : RoadmapBlock
}
