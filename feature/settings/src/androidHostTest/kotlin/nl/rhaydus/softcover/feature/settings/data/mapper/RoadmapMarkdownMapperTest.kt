package nl.rhaydus.softcover.feature.settings.data.mapper

import io.kotest.matchers.shouldBe

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapBlock
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSpan

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
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(text = "Body"))),
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
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(text = "Paragraph one"))),
                RoadmapBlock.Heading(
                    level = 1,
                    spans = listOf(RoadmapSpan(text = "Title")),
                ),
            )
        }

        @Test
        fun `double hash produces level two heading`() {
            // ----- Arrange -----
            val markdown = "Intro\n\n## Heading Two"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(text = "Intro"))),
                RoadmapBlock.Heading(
                    level = 2,
                    spans = listOf(RoadmapSpan(text = "Heading Two")),
                ),
            )
        }

        @Test
        fun `triple hash produces level three heading`() {
            // ----- Arrange -----
            val markdown = "Intro\n\n### Heading Three"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(text = "Intro"))),
                RoadmapBlock.Heading(
                    level = 3,
                    spans = listOf(RoadmapSpan(text = "Heading Three")),
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
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(text = "Line one Line two Line three"))),
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
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(text = "First paragraph text"))),
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(text = "Second paragraph text"))),
            )
        }
    }

    @Nested
    inner class Lists {
        @Test
        fun `dash bullet produces a bullet list item`() {
            // ----- Arrange -----
            val markdown = "- First item"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.BulletListItem(spans = listOf(RoadmapSpan(text = "First item"))),
            )
        }

        @Test
        fun `asterisk bullet produces a bullet list item`() {
            // ----- Arrange -----
            val markdown = "* First item"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.BulletListItem(spans = listOf(RoadmapSpan(text = "First item"))),
            )
        }

        @Test
        fun `ordered item produces the parsed index`() {
            // ----- Arrange -----
            val markdown = "1. First item"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.OrderedListItem(
                    index = 1,
                    spans = listOf(RoadmapSpan(text = "First item")),
                ),
            )
        }

        @Test
        fun `non-sequential explicit index is preserved`() {
            // ----- Arrange -----
            val markdown = "5. Fifth item"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.OrderedListItem(
                    index = 5,
                    spans = listOf(RoadmapSpan(text = "Fifth item")),
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
                RoadmapBlock.BlockQuote(spans = listOf(RoadmapSpan(text = "Line one Line two"))),
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
                RoadmapBlock.BlockQuote(spans = listOf(RoadmapSpan(text = "Quoted text"))),
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(text = "Plain paragraph"))),
            )
        }
    }

    @Nested
    inner class ThematicBreaks {
        @Test
        fun `three dashes produce a thematic break`() {
            // ----- Arrange -----
            val markdown = "---"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(RoadmapBlock.ThematicBreak)
        }

        @Test
        fun `three asterisks produce a thematic break`() {
            // ----- Arrange -----
            val markdown = "***"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(RoadmapBlock.ThematicBreak)
        }

        @Test
        fun `three underscores produce a thematic break`() {
            // ----- Arrange -----
            val markdown = "___"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(RoadmapBlock.ThematicBreak)
        }

        @Test
        fun `more than three repeated dashes still produce a thematic break`() {
            // ----- Arrange -----
            val markdown = "-----"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(RoadmapBlock.ThematicBreak)
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
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(
                    text = "bold text",
                    bold = true,
                ),),),
            )
        }

        @Test
        fun `italic text with asterisks produces an italic span`() {
            // ----- Arrange -----
            val markdown = "*italic text*"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(
                    text = "italic text",
                    italic = true,
                ),),),
            )
        }

        @Test
        fun `italic text with underscores produces an italic span`() {
            // ----- Arrange -----
            val markdown = "_italic text_"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(
                    text = "italic text",
                    italic = true,
                ),),),
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
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(
                    text = "code text",
                    code = true,
                ),),),
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
                    spans = listOf(RoadmapSpan(
                        text = "Click here",
                        url = "https://example.com",
                    ),),
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
                    spans = listOf(
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
                    spans = listOf(
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
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(text = "*"))),
            )
        }
    }

    @Nested
    inner class Robustness {
        @Test
        fun `empty string produces an empty block list`() {
            // ----- Arrange -----
            val markdown = ""

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe emptyList()
        }

        @Test
        fun `whitespace-only string produces an empty block list`() {
            // ----- Arrange -----
            val markdown = "   \n  \n"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe emptyList()
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
        fun `a markdown table row survives as plain paragraph text`() {
            // ----- Arrange -----
            val markdown = "| a | b |"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(text = "| a | b |"))),
            )
        }

        @Test
        fun `a raw html tag survives as plain paragraph text`() {
            // ----- Arrange -----
            val markdown = "<div>text</div>"

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                RoadmapBlock.Paragraph(spans = listOf(RoadmapSpan(text = "<div>text</div>"))),
            )
        }
    }

    @Nested
    inner class RealRoadmapExcerpt {
        @Test
        fun `parses a realistic excerpt of the actual roadmap document`() {
            // ----- Arrange -----
            val markdown = """
                # Softcover roadmap

                What we're building next for [Softcover](https://hardcover.app/), grouped by the version it's planned to land in. This is the public view of our plan.

                **Current release: 3.1.2.** Softcover runs on Android, iPhone, iPad and desktop.

                A few honest caveats:
                - **These are plans, not promises.** Versions are listed in the order we intend to ship them, but order can shift and some items may move, merge, or change shape.
                - **No release dates.** We ship when each drop is ready.

                > Reading this inside the app? It's the same file, fetched live, so it's always current.

                ---

                ## 3.2.0: Widgets, and a lot of small things

                **Home-screen widgets** (Android first): the book you're **Currently Reading**, a **random pick from Want-to-Read** for when you can't decide.
            """.trimIndent()

            // ----- Act -----
            val blocks = markdown.toRoadmapBlocks()

            // ----- Assert -----
            val firstBlock = blocks.first()

            (firstBlock is RoadmapBlock.Heading && firstBlock.level == 1) shouldBe false

            val introParagraph = firstBlock as RoadmapBlock.Paragraph
            introParagraph.spans.any { it.text.contains("What we're building next") } shouldBe true

            val levelTwoHeadings = blocks.filterIsInstance<RoadmapBlock.Heading>()
                .filter { it.level == 2 }
            levelTwoHeadings.isNotEmpty() shouldBe true
        }
    }
}
