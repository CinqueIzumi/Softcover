package nl.rhaydus.softcover.feature.settings.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapBlock
import nl.rhaydus.softcover.feature.settings.presentation.model.RoadmapRenderUnit

internal fun List<RoadmapBlock>.toRoadmapRenderUnits(): ImmutableList<RoadmapRenderUnit> {
    val units = mutableListOf<RoadmapRenderUnit>()
    var index = 0

    while (index < size) {
        when (val block = this[index]) {
            is RoadmapBlock.Heading -> {
                units += block.toRenderUnit()
                index += 1
            }

            is RoadmapBlock.BulletListItem, is RoadmapBlock.OrderedListItem -> {
                val runStart = index

                while (
                    index < size &&
                    (this[index] is RoadmapBlock.BulletListItem || this[index] is RoadmapBlock.OrderedListItem)
                ) {
                    index += 1
                }

                units += RoadmapRenderUnit.ListGroup(
                    items = subList(
                        runStart,
                        index,
                    ),
                )
            }

            is RoadmapBlock.Paragraph -> {
                units += RoadmapRenderUnit.Paragraph(spans = block.spans)
                index += 1
            }

            is RoadmapBlock.BlockQuote -> {
                units += RoadmapRenderUnit.BlockQuote(spans = block.spans)
                index += 1
            }

            RoadmapBlock.ThematicBreak -> {
                units += RoadmapRenderUnit.ThematicBreak
                index += 1
            }
        }
    }

    return units.toPersistentList()
}

/**
 * A heading shaped `"3.2.0: Widgets, and a lot of small things"` splits on the first `": "` into an
 * eyebrow (the version) + headline (the title); a heading without that shape (`"Under consideration"`)
 * degrades to [RoadmapRenderUnit.PlainHeading].
 */
private fun RoadmapBlock.Heading.toRenderUnit(): RoadmapRenderUnit {
    val plainText = spans.joinToString(separator = "") { it.text }
    val separatorIndex = plainText.indexOf(": ")

    return if (separatorIndex > 0 && separatorIndex < plainText.length - 2) {
        RoadmapRenderUnit.Heading(
            header = SectionHeaderUiModel.Section(
                eyebrow = plainText.substring(
                    startIndex = 0,
                    endIndex = separatorIndex,
                ),
                headline = plainText.substring(startIndex = separatorIndex + 2),
            ),
        )
    } else {
        RoadmapRenderUnit.PlainHeading(text = plainText)
    }
}
