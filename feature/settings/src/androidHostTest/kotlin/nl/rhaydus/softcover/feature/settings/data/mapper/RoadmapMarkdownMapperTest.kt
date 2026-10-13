package nl.rhaydus.softcover.feature.settings.data.mapper

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapBlock
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSpan
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RoadmapMarkdownMapperTest {
    @Nested
    inner class Headings {
        @Test
        fun `leading h1 is dropped`() {
            // ----- Arrange -----
            val markdown = "# Title\n\nBody"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(spans = persistentListOf(RoadmapSpan(text = "Body"))),
            )
        }

        @Test
        fun `h1 not in first position is kept`() {
            // ----- Arrange -----
            val markdown = "Paragraph one\n\n# Title"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(spans = persistentListOf(RoadmapSpan(text = "Paragraph one"))),
                RoadmapBlock.Heading(
                    level = 1,
                    spans = persistentListOf(RoadmapSpan(text = "Title")),
                ),
            )
        }

        @Test
        fun `double and triple hashes produce level two and three headings`() {
            // ----- Arrange -----
            val markdown = "Intro\n\n## Heading Two\n\n### Heading Three"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(spans = persistentListOf(RoadmapSpan(text = "Intro"))),
                RoadmapBlock.Heading(
                    level = 2,
                    spans = persistentListOf(RoadmapSpan(text = "Heading Two")),
                ),
                RoadmapBlock.Heading(
                    level = 3,
                    spans = persistentListOf(RoadmapSpan(text = "Heading Three")),
                ),
            )
        }
    }

    @Nested
    inner class Paragraphs {
        @Test
        fun `soft-wrapped lines join into a single paragraph`() {
            // ----- Arrange -----
            val markdown = "Line one\nLine two\nLine three"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(spans = persistentListOf(RoadmapSpan(text = "Line one Line two Line three"))),
            )
        }

        @Test
        fun `blank line separates two paragraphs`() {
            // ----- Arrange -----
            val markdown = "First paragraph text\n\nSecond paragraph text"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(spans = persistentListOf(RoadmapSpan(text = "First paragraph text"))),
                RoadmapBlock.Paragraph(spans = persistentListOf(RoadmapSpan(text = "Second paragraph text"))),
            )
        }
    }

    @Nested
    inner class Lists {
        @Test
        fun `dash and asterisk bullets produce bullet list items`() {
            // ----- Arrange -----
            val markdown = "- First item\n* Second item"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.BulletListItem(spans = persistentListOf(RoadmapSpan(text = "First item"))),
                RoadmapBlock.BulletListItem(spans = persistentListOf(RoadmapSpan(text = "Second item"))),
            )
        }

        @Test
        fun `ordered items keep their explicit non-sequential index`() {
            // ----- Arrange -----
            val markdown = "1. First item\n5. Fifth item"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.OrderedListItem(
                    index = 1,
                    spans = persistentListOf(RoadmapSpan(text = "First item")),
                ),
                RoadmapBlock.OrderedListItem(
                    index = 5,
                    spans = persistentListOf(RoadmapSpan(text = "Fifth item")),
                ),
            )
        }
    }

    @Nested
    inner class BlockQuotes {
        @Test
        fun `consecutive quote lines merge into a single block quote`() {
            // ----- Arrange -----
            val markdown = "> Line one\n> Line two"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.BlockQuote(spans = persistentListOf(RoadmapSpan(text = "Line one Line two"))),
            )
        }

        @Test
        fun `a quote followed by a non-quote line ends the quote`() {
            // ----- Arrange -----
            val markdown = "> Quoted text\nPlain paragraph"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.BlockQuote(spans = persistentListOf(RoadmapSpan(text = "Quoted text"))),
                RoadmapBlock.Paragraph(spans = persistentListOf(RoadmapSpan(text = "Plain paragraph"))),
            )
        }
    }

    @Nested
    inner class ThematicBreaks {
        @Test
        fun `three or more dashes, asterisks or underscores produce a thematic break`() {
            // ----- Arrange -----
            val markdown = "---\n***\n___\n-----"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.ThematicBreak,
                RoadmapBlock.ThematicBreak,
                RoadmapBlock.ThematicBreak,
                RoadmapBlock.ThematicBreak,
            )
        }
    }

    @Nested
    inner class Spans {
        @Test
        fun `bold text produces a bold span`() {
            // ----- Arrange -----
            val markdown = "**bold text**"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(
                    spans = persistentListOf(
                        RoadmapSpan(
                            text = "bold text",
                            bold = true,
                        ),
                    ),
                ),
            )
        }

        @Test
        fun `italic text with asterisks or underscores produces italic spans`() {
            // ----- Arrange -----
            val markdown = "*one* _two_"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(
                    spans = persistentListOf(
                        RoadmapSpan(
                            text = "one",
                            italic = true,
                        ),
                        RoadmapSpan(text = " "),
                        RoadmapSpan(
                            text = "two",
                            italic = true,
                        ),
                    ),
                ),
            )
        }

        @Test
        fun `backtick text produces a code span`() {
            // ----- Arrange -----
            val markdown = "`code text`"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(
                    spans = persistentListOf(
                        RoadmapSpan(
                            text = "code text",
                            code = true,
                        ),
                    ),
                ),
            )
        }

        @Test
        fun `a markdown link produces a span with text and url`() {
            // ----- Arrange -----
            val markdown = "[Click here](https://example.com)"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(
                    spans = persistentListOf(
                        RoadmapSpan(
                            text = "Click here",
                            url = "https://example.com",
                        ),
                    ),
                ),
            )
        }

        @Test
        fun `bold plain and link text combine into ordered spans on one line`() {
            // ----- Arrange -----
            val markdown = "**Bold** plain [text](http://x.com)"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(
                    spans = persistentListOf(
                        RoadmapSpan(
                            text = "Bold",
                            bold = true,
                        ),
                        RoadmapSpan(text = " plain "),
                        RoadmapSpan(
                            text = "text",
                            url = "http://x.com",
                        ),
                    ),
                ),
            )
        }

        @Test
        fun `an unterminated bold marker still yields the trailing text as a bold span`() {
            // ----- Arrange -----
            val markdown = "Plain text **bold forever"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(
                    spans = persistentListOf(
                        RoadmapSpan(text = "Plain text "),
                        RoadmapSpan(
                            text = "bold forever",
                            bold = true,
                        ),
                    ),
                ),
            )
        }

        @Test
        fun `a lone marker with no content falls back to the original raw text`() {
            // ----- Arrange -----
            val markdown = "*"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(spans = persistentListOf(RoadmapSpan(text = "*"))),
            )
        }
    }

    @Nested
    inner class Robustness {
        @Test
        fun `empty and whitespace-only strings produce an empty block list`() {
            // ----- Arrange -----
            val empty = ""
            val whitespace = "   \n  \n"

            // ----- Act -----
            val emptyBlocks = empty.toRoadmapBlocks()
            val whitespaceBlocks = whitespace.toRoadmapBlocks()

            // ----- Assert -----
            emptyBlocks shouldBe emptyList()
            whitespaceBlocks shouldBe emptyList()
        }

        @Test
        fun `crlf line endings parse identically to lf`() {
            // ----- Arrange -----
            val crlf = "Line one\r\n\r\nLine two"
            val lf = "Line one\n\nLine two"

            // ----- Act -----
            val crlfBlocks = crlf.toRoadmapBlocks()
            val lfBlocks = lf.toRoadmapBlocks()

            // ----- Assert -----
            crlfBlocks shouldBe lfBlocks
        }

        @Test
        fun `unsupported table and html syntax survive as plain paragraph text`() {
            // ----- Arrange -----
            val markdown = "| a | b |\n\n<div>text</div>"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(spans = persistentListOf(RoadmapSpan(text = "| a | b |"))),
                RoadmapBlock.Paragraph(spans = persistentListOf(RoadmapSpan(text = "<div>text</div>"))),
            )
        }
    }
}
