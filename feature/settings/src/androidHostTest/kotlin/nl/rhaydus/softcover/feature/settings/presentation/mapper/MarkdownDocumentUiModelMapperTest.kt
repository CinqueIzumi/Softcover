package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentBlockUiModel
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentListItemUiModel
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentSpanUiModel
import nl.rhaydus.softcover.core.component.document.MarkdownDocumentUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownBlock
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownSpan
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MarkdownDocumentUiModelMapperTest {
    @Nested
    inner class Headings {
        @Test
        fun `heading splits on the first colon-space into eyebrow and headline`() {
            // ----- Arrange -----
            val blocks = listOf(
                MarkdownBlock.Heading(
                    level = 2,
                    spans = persistentListOf(MarkdownSpan(text = "3.2.0: Widgets: and more")),
                ),
            )

            // ----- Act -----
            val model = blocks.toMarkdownDocumentUiModel()

            // ----- Assert -----
            model shouldBe MarkdownDocumentUiModel(
                blocks = persistentListOf(
                    MarkdownDocumentBlockUiModel.SectionHeading(
                        header = SectionHeaderUiModel.Section(
                            eyebrow = "3.2.0",
                            headline = "Widgets: and more",
                        ),
                    ),
                ),
            )
        }

        @Test
        fun `heading with no eyebrow or no headline around the colon-space degrades to a plain heading`() {
            listOf(
                "Under consideration",
                ": Title",
                "Title: ",
            ).forEach { text ->
                // ----- Arrange -----
                val blocks = listOf(
                    MarkdownBlock.Heading(
                        level = 2,
                        spans = persistentListOf(MarkdownSpan(text = text)),
                    ),
                )

                // ----- Act -----
                val model = blocks.toMarkdownDocumentUiModel()

                // ----- Assert -----
                model.blocks shouldBe persistentListOf(MarkdownDocumentBlockUiModel.PlainHeading(text = text))
            }
        }

        @Test
        fun `heading spans are joined into plain text before the split`() {
            // ----- Arrange -----
            val blocks = listOf(
                MarkdownBlock.Heading(
                    level = 2,
                    spans = persistentListOf(
                        MarkdownSpan(
                            text = "3.2.0",
                            bold = true,
                        ),
                        MarkdownSpan(text = ": Widgets"),
                    ),
                ),
            )

            // ----- Act -----
            val model = blocks.toMarkdownDocumentUiModel()

            // ----- Assert -----
            model.blocks shouldBe persistentListOf(
                MarkdownDocumentBlockUiModel.SectionHeading(
                    header = SectionHeaderUiModel.Section(
                        eyebrow = "3.2.0",
                        headline = "Widgets",
                    ),
                ),
            )
        }

        @Test
        fun `a colon-space heading outside level two degrades to a plain heading`() {
            listOf(1, 3, 4).forEach { level ->
                // ----- Arrange -----
                val blocks = listOf(
                    MarkdownBlock.Heading(
                        level = level,
                        spans = persistentListOf(MarkdownSpan(text = "Fixed: the crash")),
                    ),
                )

                // ----- Act -----
                val model = blocks.toMarkdownDocumentUiModel()

                // ----- Assert -----
                model.blocks shouldBe persistentListOf(
                    MarkdownDocumentBlockUiModel.PlainHeading(text = "Fixed: the crash"),
                )
            }
        }

        @Test
        fun `only the level two heading splits when levels are mixed`() {
            // ----- Arrange -----
            val blocks = listOf(
                MarkdownBlock.Heading(
                    level = 2,
                    spans = persistentListOf(MarkdownSpan(text = "3.2.0: Fresh shelves")),
                ),
                MarkdownBlock.Heading(
                    level = 3,
                    spans = persistentListOf(MarkdownSpan(text = "Fixed: the crash")),
                ),
            )

            // ----- Act -----
            val model = blocks.toMarkdownDocumentUiModel()

            // ----- Assert -----
            model.blocks shouldBe persistentListOf(
                MarkdownDocumentBlockUiModel.SectionHeading(
                    header = SectionHeaderUiModel.Section(
                        eyebrow = "3.2.0",
                        headline = "Fresh shelves",
                    ),
                ),
                MarkdownDocumentBlockUiModel.PlainHeading(text = "Fixed: the crash"),
            )
        }
    }

    @Nested
    inner class Lists {
        @Test
        fun `consecutive bullet and ordered items merge into one list group with their markers`() {
            // ----- Arrange -----
            val blocks = listOf(
                MarkdownBlock.BulletListItem(spans = persistentListOf(MarkdownSpan(text = "First"))),
                MarkdownBlock.OrderedListItem(
                    index = 7,
                    spans = persistentListOf(MarkdownSpan(text = "Second")),
                ),
            )

            // ----- Act -----
            val model = blocks.toMarkdownDocumentUiModel()

            // ----- Assert -----
            model.blocks shouldBe persistentListOf(
                MarkdownDocumentBlockUiModel.ListGroup(
                    items = persistentListOf(
                        MarkdownDocumentListItemUiModel(
                            marker = "•",
                            spans = persistentListOf(MarkdownDocumentSpanUiModel(text = "First")),
                        ),
                        MarkdownDocumentListItemUiModel(
                            marker = "7.",
                            spans = persistentListOf(MarkdownDocumentSpanUiModel(text = "Second")),
                        ),
                    ),
                ),
            )
        }

        @Test
        fun `a non-list block splits an otherwise consecutive list run into separate list groups`() {
            // ----- Arrange -----
            val blocks = listOf(
                MarkdownBlock.BulletListItem(spans = persistentListOf(MarkdownSpan(text = "First"))),
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "Between"))),
                MarkdownBlock.BulletListItem(spans = persistentListOf(MarkdownSpan(text = "Second"))),
            )

            // ----- Act -----
            val model = blocks.toMarkdownDocumentUiModel()

            // ----- Assert -----
            model.blocks.map { it::class } shouldBe listOf(
                MarkdownDocumentBlockUiModel.ListGroup::class,
                MarkdownDocumentBlockUiModel.Paragraph::class,
                MarkdownDocumentBlockUiModel.ListGroup::class,
            )
            (model.blocks[0] as MarkdownDocumentBlockUiModel.ListGroup).items.size shouldBe 1
            (model.blocks[2] as MarkdownDocumentBlockUiModel.ListGroup).items.size shouldBe 1
        }

        @Test
        fun `a heading between two list items also splits the run`() {
            // ----- Arrange -----
            val blocks = listOf(
                MarkdownBlock.BulletListItem(spans = persistentListOf(MarkdownSpan(text = "A"))),
                MarkdownBlock.Heading(
                    level = 2,
                    spans = persistentListOf(MarkdownSpan(text = "Next")),
                ),
                MarkdownBlock.BulletListItem(spans = persistentListOf(MarkdownSpan(text = "B"))),
            )

            // ----- Act -----
            val model = blocks.toMarkdownDocumentUiModel()

            // ----- Assert -----
            model.blocks.size shouldBe 3
        }
    }

    @Nested
    inner class Blocks {
        @Test
        fun `paragraph blockquote and thematic break map one to one`() {
            // ----- Arrange -----
            val blocks = listOf(
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "Body"))),
                MarkdownBlock.BlockQuote(spans = persistentListOf(MarkdownSpan(text = "Quoted"))),
                MarkdownBlock.ThematicBreak,
            )

            // ----- Act -----
            val model = blocks.toMarkdownDocumentUiModel()

            // ----- Assert -----
            model.blocks shouldBe persistentListOf(
                MarkdownDocumentBlockUiModel.Paragraph(
                    spans = persistentListOf(MarkdownDocumentSpanUiModel(text = "Body")),
                ),
                MarkdownDocumentBlockUiModel.BlockQuote(
                    spans = persistentListOf(MarkdownDocumentSpanUiModel(text = "Quoted")),
                ),
                MarkdownDocumentBlockUiModel.ThematicBreak,
            )
        }

        @Test
        fun `every span flag and the url carry over to the span ui model`() {
            // ----- Arrange -----
            val blocks = listOf(
                MarkdownBlock.Paragraph(
                    spans = persistentListOf(
                        MarkdownSpan(
                            text = "all",
                            bold = true,
                            italic = true,
                            code = true,
                            url = "https://example.com",
                        ),
                        MarkdownSpan(text = "plain"),
                    ),
                ),
            )

            // ----- Act -----
            val model = blocks.toMarkdownDocumentUiModel()

            // ----- Assert -----
            model.blocks shouldBe persistentListOf(
                MarkdownDocumentBlockUiModel.Paragraph(
                    spans = persistentListOf(
                        MarkdownDocumentSpanUiModel(
                            text = "all",
                            bold = true,
                            italic = true,
                            code = true,
                            url = "https://example.com",
                        ),
                        MarkdownDocumentSpanUiModel(text = "plain"),
                    ),
                ),
            )
        }

        @Test
        fun `an empty block list produces an empty document that is not loading`() {
            // ----- Arrange -----
            val blocks = emptyList<MarkdownBlock>()

            // ----- Act -----
            val model = blocks.toMarkdownDocumentUiModel()

            // ----- Assert -----
            model shouldBe MarkdownDocumentUiModel(blocks = persistentListOf())
        }
    }
}
