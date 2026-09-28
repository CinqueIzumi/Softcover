package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapBlock
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSpan
import nl.rhaydus.softcover.feature.settings.presentation.model.RoadmapRenderUnit
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RoadmapRenderUnitMapperTest {
    @Nested
    inner class ToRoadmapRenderUnits {
        @Test
        fun `heading shaped version colon title splits on the first colon-space into eyebrow and headline`() {
            // ----- Arrange -----
            val blocks = listOf(
                RoadmapBlock.Heading(
                    level = 2,
                    spans = listOf(RoadmapSpan(text = "3.2.0: Title")),
                ),
            )

            // ----- Act -----
            val units = blocks.toRoadmapRenderUnits()

            // ----- Assert -----
            units shouldBe persistentListOf(
                RoadmapRenderUnit.Heading(
                    header = SectionHeaderUiModel.Section(
                        eyebrow = "3.2.0",
                        headline = "Title",
                    ),
                ),
            )
        }

        @Test
        fun `heading with a later colon-space still splits on the first occurrence only`() {
            // ----- Arrange -----
            val blocks = listOf(
                RoadmapBlock.Heading(
                    level = 2,
                    spans = listOf(RoadmapSpan(text = "3.2.0: Widgets: and more")),
                ),
            )

            // ----- Act -----
            val units = blocks.toRoadmapRenderUnits()

            // ----- Assert -----
            units shouldBe persistentListOf(
                RoadmapRenderUnit.Heading(
                    header = SectionHeaderUiModel.Section(
                        eyebrow = "3.2.0",
                        headline = "Widgets: and more",
                    ),
                ),
            )
        }

        @Test
        fun `heading without a colon-space degrades to a plain heading`() {
            // ----- Arrange -----
            val blocks = listOf(
                RoadmapBlock.Heading(
                    level = 2,
                    spans = listOf(RoadmapSpan(text = "Under consideration")),
                ),
            )

            // ----- Act -----
            val units = blocks.toRoadmapRenderUnits()

            // ----- Assert -----
            units shouldBe persistentListOf(RoadmapRenderUnit.PlainHeading(text = "Under consideration"))
        }

        @Test
        fun `heading starting with the colon-space degrades to a plain heading`() {
            // ----- Arrange -----
            val blocks = listOf(
                RoadmapBlock.Heading(
                    level = 2,
                    spans = listOf(RoadmapSpan(text = ": Title")),
                ),
            )

            // ----- Act -----
            val units = blocks.toRoadmapRenderUnits()

            // ----- Assert -----
            units shouldBe persistentListOf(RoadmapRenderUnit.PlainHeading(text = ": Title"))
        }

        @Test
        fun `heading ending with the colon-space and no headline degrades to a plain heading`() {
            // ----- Arrange -----
            val blocks = listOf(
                RoadmapBlock.Heading(
                    level = 2,
                    spans = listOf(RoadmapSpan(text = "Title: ")),
                ),
            )

            // ----- Act -----
            val units = blocks.toRoadmapRenderUnits()

            // ----- Assert -----
            units shouldBe persistentListOf(RoadmapRenderUnit.PlainHeading(text = "Title: "))
        }

        @Test
        fun `consecutive bullet and ordered items merge into one list group`() {
            // ----- Arrange -----
            val bullet = RoadmapBlock.BulletListItem(spans = listOf(RoadmapSpan(text = "First")))
            val ordered = RoadmapBlock.OrderedListItem(
                index = 1,
                spans = listOf(RoadmapSpan(text = "Second")),
            )
            val blocks = listOf(bullet, ordered)

            // ----- Act -----
            val units = blocks.toRoadmapRenderUnits()

            // ----- Assert -----
            units shouldBe persistentListOf(RoadmapRenderUnit.ListGroup(items = listOf(bullet, ordered)))
        }

        @Test
        fun `a non-list block splits an otherwise consecutive list run into separate list groups`() {
            // ----- Arrange -----
            val firstBullet = RoadmapBlock.BulletListItem(spans = listOf(RoadmapSpan(text = "First")))
            val paragraph = RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(text = "Between")))
            val secondBullet = RoadmapBlock.BulletListItem(spans = listOf(RoadmapSpan(text = "Second")))
            val blocks = listOf(firstBullet, paragraph, secondBullet)

            // ----- Act -----
            val units = blocks.toRoadmapRenderUnits()

            // ----- Assert -----
            units shouldBe persistentListOf(
                RoadmapRenderUnit.ListGroup(items = listOf(firstBullet)),
                RoadmapRenderUnit.Paragraph(spans = paragraph.spans),
                RoadmapRenderUnit.ListGroup(items = listOf(secondBullet)),
            )
        }

        @Test
        fun `paragraph blockquote and thematic break map one to one`() {
            // ----- Arrange -----
            val paragraph = RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(text = "Body")))
            val blockQuote = RoadmapBlock.BlockQuote(spans = listOf(RoadmapSpan(text = "Quoted")))
            val blocks = listOf(paragraph, blockQuote, RoadmapBlock.ThematicBreak)

            // ----- Act -----
            val units = blocks.toRoadmapRenderUnits()

            // ----- Assert -----
            units shouldBe persistentListOf(
                RoadmapRenderUnit.Paragraph(spans = paragraph.spans),
                RoadmapRenderUnit.BlockQuote(spans = blockQuote.spans),
                RoadmapRenderUnit.ThematicBreak,
            )
        }

        @Test
        fun `an empty document maps to an empty list`() {
            // ----- Arrange -----
            val blocks = emptyList<RoadmapBlock>()

            // ----- Act -----
            val units = blocks.toRoadmapRenderUnits()

            // ----- Assert -----
            units shouldBe persistentListOf()
        }
    }
}
