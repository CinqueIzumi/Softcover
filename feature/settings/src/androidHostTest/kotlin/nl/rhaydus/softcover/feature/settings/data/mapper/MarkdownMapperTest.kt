package nl.rhaydus.softcover.feature.settings.data.mapper

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownBlock
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownSpan
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MarkdownMapperTest {
    @Nested
    inner class Headings {
        @Test
        fun `leading h1 is dropped`() {
            // ----- Arrange -----
            val markdown = "# Title\n\nBody"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "Body"))),
            )
        }

        @Test
        fun `h1 not in first position is kept`() {
            // ----- Arrange -----
            val markdown = "Paragraph one\n\n# Title"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "Paragraph one"))),
                MarkdownBlock.Heading(
                    level = 1,
                    spans = persistentListOf(MarkdownSpan(text = "Title")),
                ),
            )
        }

        @Test
        fun `double and triple hashes produce level two and three headings`() {
            // ----- Arrange -----
            val markdown = "Intro\n\n## Heading Two\n\n### Heading Three"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "Intro"))),
                MarkdownBlock.Heading(
                    level = 2,
                    spans = persistentListOf(MarkdownSpan(text = "Heading Two")),
                ),
                MarkdownBlock.Heading(
                    level = 3,
                    spans = persistentListOf(MarkdownSpan(text = "Heading Three")),
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
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "Line one Line two Line three"))),
            )
        }

        @Test
        fun `blank line separates two paragraphs`() {
            // ----- Arrange -----
            val markdown = "First paragraph text\n\nSecond paragraph text"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "First paragraph text"))),
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "Second paragraph text"))),
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
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.BulletListItem(spans = persistentListOf(MarkdownSpan(text = "First item"))),
                MarkdownBlock.BulletListItem(spans = persistentListOf(MarkdownSpan(text = "Second item"))),
            )
        }

        @Test
        fun `ordered items keep their explicit non-sequential index`() {
            // ----- Arrange -----
            val markdown = "1. First item\n5. Fifth item"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.OrderedListItem(
                    index = 1,
                    spans = persistentListOf(MarkdownSpan(text = "First item")),
                ),
                MarkdownBlock.OrderedListItem(
                    index = 5,
                    spans = persistentListOf(MarkdownSpan(text = "Fifth item")),
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
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.BlockQuote(spans = persistentListOf(MarkdownSpan(text = "Line one Line two"))),
            )
        }

        @Test
        fun `a quote followed by a non-quote line ends the quote`() {
            // ----- Arrange -----
            val markdown = "> Quoted text\nPlain paragraph"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.BlockQuote(spans = persistentListOf(MarkdownSpan(text = "Quoted text"))),
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "Plain paragraph"))),
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
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.ThematicBreak,
                MarkdownBlock.ThematicBreak,
                MarkdownBlock.ThematicBreak,
                MarkdownBlock.ThematicBreak,
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
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(
                    spans = persistentListOf(
                        MarkdownSpan(
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
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(
                    spans = persistentListOf(
                        MarkdownSpan(
                            text = "one",
                            italic = true,
                        ),
                        MarkdownSpan(text = " "),
                        MarkdownSpan(
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
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(
                    spans = persistentListOf(
                        MarkdownSpan(
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
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(
                    spans = persistentListOf(
                        MarkdownSpan(
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
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(
                    spans = persistentListOf(
                        MarkdownSpan(
                            text = "Bold",
                            bold = true,
                        ),
                        MarkdownSpan(text = " plain "),
                        MarkdownSpan(
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
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(
                    spans = persistentListOf(
                        MarkdownSpan(text = "Plain text "),
                        MarkdownSpan(
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
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "*"))),
            )
        }
    }

    @Nested
    inner class Underscores {
        @Test
        fun `an intra-word underscore stays literal text`() {
            // ----- Arrange -----
            val markdown = "Use snake_case and file_name_2 here"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(
                    spans = persistentListOf(MarkdownSpan(text = "Use snake_case and file_name_2 here")),
                ),
            )
        }

        @Test
        fun `underscores at word boundaries still toggle italic`() {
            // ----- Arrange -----
            val markdown = "a _word_ b"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(
                    spans = persistentListOf(
                        MarkdownSpan(text = "a "),
                        MarkdownSpan(
                            text = "word",
                            italic = true,
                        ),
                        MarkdownSpan(text = " b"),
                    ),
                ),
            )
        }

        @Test
        fun `italic at the start and end of the text is recognised`() {
            // ----- Arrange -----
            val markdown = "_whole_"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(
                    spans = persistentListOf(
                        MarkdownSpan(
                            text = "whole",
                            italic = true,
                        ),
                    ),
                ),
            )
        }

        @Test
        fun `intra-word and boundary underscores in one paragraph are told apart`() {
            // ----- Arrange -----
            val markdown = "Set my_var to _on_ today"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(
                    spans = persistentListOf(
                        MarkdownSpan(text = "Set my_var to "),
                        MarkdownSpan(
                            text = "on",
                            italic = true,
                        ),
                        MarkdownSpan(text = " today"),
                    ),
                ),
            )
        }

        @Test
        fun `an underscore next to punctuation counts as a boundary`() {
            // ----- Arrange -----
            val markdown = "(_hi_)"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(
                    spans = persistentListOf(
                        MarkdownSpan(text = "("),
                        MarkdownSpan(
                            text = "hi",
                            italic = true,
                        ),
                        MarkdownSpan(text = ")"),
                    ),
                ),
            )
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `emphasis markers inside a code span stay literal`() {
            // ----- Arrange -----
            val markdown = "`a*b**c`"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(
                    spans = persistentListOf(
                        MarkdownSpan(
                            text = "a*b**c",
                            code = true,
                        ),
                    ),
                ),
            )
        }

        @Test
        fun `a link inside bold carries the bold flag`() {
            // ----- Arrange -----
            val markdown = "**[site](https://example.com)**"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(
                    spans = persistentListOf(
                        MarkdownSpan(
                            text = "site",
                            bold = true,
                            url = "https://example.com",
                        ),
                    ),
                ),
            )
        }

        @Test
        fun `a heading deeper than level three is kept as paragraph text`() {
            // ----- Arrange -----
            val markdown = "#### Too deep"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "#### Too deep"))),
            )
        }

        @Test
        fun `an ordered index too large for an Int restarts counting within its own list run`() {
            // ----- Arrange -----
            val markdown = "1. One\n99999999999999999999. Overflow\n\nBreak\n\n99999999999999999999. Fresh"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.OrderedListItem(
                    index = 1,
                    spans = persistentListOf(MarkdownSpan(text = "One")),
                ),
                MarkdownBlock.OrderedListItem(
                    index = 2,
                    spans = persistentListOf(MarkdownSpan(text = "Overflow")),
                ),
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "Break"))),
                MarkdownBlock.OrderedListItem(
                    index = 1,
                    spans = persistentListOf(MarkdownSpan(text = "Fresh")),
                ),
            )
        }

        @Test
        fun `a list item directly after paragraph text ends that paragraph`() {
            // ----- Arrange -----
            val markdown = "Intro line\n- Item"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "Intro line"))),
                MarkdownBlock.BulletListItem(spans = persistentListOf(MarkdownSpan(text = "Item"))),
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
            val emptyBlocks = empty.toMarkdownBlocks()
            val whitespaceBlocks = whitespace.toMarkdownBlocks()

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
            val crlfBlocks = crlf.toMarkdownBlocks()
            val lfBlocks = lf.toMarkdownBlocks()

            // ----- Assert -----
            crlfBlocks shouldBe lfBlocks
        }

        @Test
        fun `unsupported table and html syntax survive as plain paragraph text`() {
            // ----- Arrange -----
            val markdown = "| a | b |\n\n<div>text</div>"

            // ----- Act -----
            val blocks = markdown.toMarkdownBlocks()

            // ----- Assert -----
            blocks shouldBe listOf(
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "| a | b |"))),
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "<div>text</div>"))),
            )
        }
    }
}
