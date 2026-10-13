package nl.rhaydus.softcover.feature.settings.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/**
 * One structural unit of a parsed [RoadmapDocument], in source order. List items are flat, top-level
 * blocks rather than grouped into a list container - [RoadmapMarkdownMapper][nl.rhaydus.softcover.feature.settings.data.mapper]
 * emits them one per line, and grouping consecutive [BulletListItem] / [OrderedListItem] runs into a
 * rendered `<ul>`/`<ol>` is left to the renderer, which already walks the block sequence.
 */
@Immutable
sealed interface RoadmapBlock {
    data class Heading(
        val level: Int,
        val spans: ImmutableList<RoadmapSpan>,
    ) : RoadmapBlock

    data class Paragraph(val spans: ImmutableList<RoadmapSpan>) : RoadmapBlock

    data class BulletListItem(val spans: ImmutableList<RoadmapSpan>) : RoadmapBlock

    data class OrderedListItem(
        val index: Int,
        val spans: ImmutableList<RoadmapSpan>,
    ) : RoadmapBlock

    data class BlockQuote(val spans: ImmutableList<RoadmapSpan>) : RoadmapBlock

    data object ThematicBreak : RoadmapBlock
}
