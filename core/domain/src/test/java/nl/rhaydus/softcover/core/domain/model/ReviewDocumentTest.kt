package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReviewDocumentTest {

    // ----- isBlank -----

    @Nested
    inner class IsBlank {

        @Test
        fun `EMPTY document is blank`() {
            // ----- Act & Assert -----
            ReviewDocument.EMPTY.isBlank() shouldBe true
        }

        @Test
        fun `document with no paragraphs is blank`() {
            // ----- Arrange -----
            val doc = ReviewDocument(paragraphs = emptyList())

            // ----- Act & Assert -----
            doc.isBlank() shouldBe true
        }

        @Test
        fun `document where all runs are empty strings is blank`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(ReviewRun(""), ReviewRun(""))),
                ReviewParagraph(listOf(ReviewRun(""))),
            ))

            // ----- Act & Assert -----
            doc.isBlank() shouldBe true
        }

        @Test
        fun `document where all runs are whitespace-only is blank`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(ReviewRun("   "), ReviewRun("\t"))),
            ))

            // ----- Act & Assert -----
            doc.isBlank() shouldBe true
        }

        @Test
        fun `document with at least one non-blank run is not blank`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(ReviewRun(""), ReviewRun("hello"))),
            ))

            // ----- Act & Assert -----
            doc.isBlank() shouldBe false
        }

        @Test
        fun `document with paragraph with no runs is blank`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(ReviewParagraph(runs = emptyList())))

            // ----- Act & Assert -----
            doc.isBlank() shouldBe true
        }
    }

    // ----- plainText -----

    @Nested
    inner class PlainText {

        @Test
        fun `EMPTY document produces empty string`() {
            // ----- Act & Assert -----
            ReviewDocument.EMPTY.plainText() shouldBe ""
        }

        @Test
        fun `single paragraph with single run produces the run text`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Hello")))))

            // ----- Act & Assert -----
            doc.plainText() shouldBe "Hello"
        }

        @Test
        fun `single paragraph with multiple runs concatenates runs without separator`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(
                    ReviewRun("Hello"),
                    ReviewRun(", "),
                    ReviewRun("world"),
                )),
            ))

            // ----- Act & Assert -----
            doc.plainText() shouldBe "Hello, world"
        }

        @Test
        fun `multiple paragraphs are joined by newline`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(ReviewRun("First paragraph"))),
                ReviewParagraph(listOf(ReviewRun("Second paragraph"))),
            ))

            // ----- Act & Assert -----
            doc.plainText() shouldBe "First paragraph\nSecond paragraph"
        }

        @Test
        fun `three paragraphs produce two newline separators`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(ReviewRun("P1"))),
                ReviewParagraph(listOf(ReviewRun("P2"))),
                ReviewParagraph(listOf(ReviewRun("P3"))),
            ))

            // ----- Act & Assert -----
            doc.plainText() shouldBe "P1\nP2\nP3"
        }

        @Test
        fun `paragraph with no runs contributes empty string in the join`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(ReviewRun("Before"))),
                ReviewParagraph(runs = emptyList()),
                ReviewParagraph(listOf(ReviewRun("After"))),
            ))

            // ----- Act & Assert -----
            doc.plainText() shouldBe "Before\n\nAfter"
        }

        @Test
        fun `spoiler run text is included — plainText concatenates all runs regardless of spoiler mark`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(
                    ReviewRun("Visible text"),
                    ReviewRun(" hidden part", spoiler = true),
                    ReviewRun(" more visible"),
                )),
            ))

            // ----- Act & Assert -----
            doc.plainText() shouldBe "Visible text hidden part more visible"
        }
    }

    // ----- canonical -----

    @Nested
    inner class Canonical {

        @Test
        fun `empty document stays empty`() {
            // ----- Act & Assert -----
            ReviewDocument.EMPTY.canonical() shouldBe ReviewDocument.EMPTY
        }

        @Test
        fun `document with only empty runs produces paragraph with no runs`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(ReviewRun(""), ReviewRun(""))),
            ))

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs[0].runs shouldBe emptyList()
        }

        @Test
        fun `adjacent runs with identical marks are merged`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(
                    ReviewRun("Hello"),
                    ReviewRun(" world"),
                )),
            ))

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs[0].runs.size shouldBe 1
            result.paragraphs[0].runs[0].text shouldBe "Hello world"
        }

        @Test
        fun `adjacent bold runs are merged`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(
                    ReviewRun("Foo", bold = true),
                    ReviewRun("Bar", bold = true),
                )),
            ))

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs[0].runs.size shouldBe 1
            result.paragraphs[0].runs[0].text shouldBe "FooBar"
            result.paragraphs[0].runs[0].bold shouldBe true
        }

        @Test
        fun `runs differing in bold are not merged`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(
                    ReviewRun("Plain"),
                    ReviewRun("Bold", bold = true),
                )),
            ))

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs[0].runs.size shouldBe 2
        }

        @Test
        fun `runs differing in italic are not merged`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(
                    ReviewRun("Plain"),
                    ReviewRun("Italic", italic = true),
                )),
            ))

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs[0].runs.size shouldBe 2
        }

        @Test
        fun `runs differing in spoiler are not merged`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(
                    ReviewRun("Visible"),
                    ReviewRun("Hidden", spoiler = true),
                )),
            ))

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs[0].runs.size shouldBe 2
        }

        @Test
        fun `empty runs are dropped before merging`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(
                    ReviewRun("Hello"),
                    ReviewRun(""),
                    ReviewRun(" world"),
                )),
            ))

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs[0].runs.size shouldBe 1
            result.paragraphs[0].runs[0].text shouldBe "Hello world"
        }

        @Test
        fun `canonical is idempotent — applying twice yields same result`() {
            // ----- Arrange -----
            val doc = goldenDocument()

            // ----- Act -----
            val once = doc.canonical()
            val twice = once.canonical()

            // ----- Assert -----
            twice shouldBe once
        }

        @Test
        fun `runs from different paragraphs are not merged across paragraph boundary`() {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(
                ReviewParagraph(listOf(ReviewRun("First"))),
                ReviewParagraph(listOf(ReviewRun("Second"))),
            ))

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs.size shouldBe 2
            result.paragraphs[0].runs[0].text shouldBe "First"
            result.paragraphs[1].runs[0].text shouldBe "Second"
        }

        private fun goldenDocument() = ReviewDocument(
            paragraphs = listOf(
                ReviewParagraph(runs = listOf(
                    ReviewRun(text = "Loved", bold = true),
                    ReviewRun(text = " it a ton. "),
                )),
                ReviewParagraph(runs = listOf(
                    ReviewRun(text = "Will be reading more by "),
                    ReviewRun(text = "Kylie Lee Baker", italic = true),
                    ReviewRun(text = "."),
                )),
                ReviewParagraph(runs = listOf(
                    ReviewRun(text = "Test", spoiler = true),
                )),
            ),
        )
    }
}
