package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReviewDocumentTest {
    private fun paragraph(vararg runs: ReviewRun) = ReviewParagraph(runs = runs.toList())

    private fun document(vararg paragraphs: ReviewParagraph) = ReviewDocument(paragraphs = paragraphs.toList())

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
            val doc = document(
                paragraph(
                    ReviewRun(""),
                    ReviewRun(""),
                ),
                paragraph(ReviewRun("")),
            )

            // ----- Act & Assert -----
            doc.isBlank() shouldBe true
        }

        @Test
        fun `document where all runs are whitespace-only is blank`() {
            // ----- Arrange -----
            val doc = document(
                paragraph(
                    ReviewRun("   "),
                    ReviewRun("\t"),
                ),
            )

            // ----- Act & Assert -----
            doc.isBlank() shouldBe true
        }

        @Test
        fun `document with at least one non-blank run is not blank`() {
            // ----- Arrange -----
            val doc = document(
                paragraph(
                    ReviewRun(""),
                    ReviewRun("hello"),
                ),
            )

            // ----- Act & Assert -----
            doc.isBlank() shouldBe false
        }

        @Test
        fun `document with paragraph with no runs is blank`() {
            // ----- Arrange -----
            val doc = document(
                paragraph(),
            )

            // ----- Act & Assert -----
            doc.isBlank() shouldBe true
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
            val doc = document(
                paragraph(
                    ReviewRun(""),
                    ReviewRun(""),
                ),
            )

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs[0].runs shouldBe emptyList()
        }

        @Test
        fun `adjacent runs with identical marks are merged`() {
            // ----- Arrange -----
            val doc = document(
                paragraph(
                    ReviewRun("Hello"),
                    ReviewRun(" world"),
                ),
            )

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs[0].runs.size shouldBe 1
            result.paragraphs[0].runs[0].text shouldBe "Hello world"
        }

        @Test
        fun `adjacent bold runs are merged`() {
            // ----- Arrange -----
            val doc = document(
                paragraph(
                    ReviewRun(
                        "Foo",
                        bold = true,
                    ),
                    ReviewRun(
                        "Bar",
                        bold = true,
                    ),
                ),
            )

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
            val doc = document(
                paragraph(
                    ReviewRun("Plain"),
                    ReviewRun(
                        "Bold",
                        bold = true,
                    ),
                ),
            )

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs[0].runs.size shouldBe 2
        }

        @Test
        fun `runs differing in italic are not merged`() {
            // ----- Arrange -----
            val doc = document(
                paragraph(
                    ReviewRun("Plain"),
                    ReviewRun(
                        "Italic",
                        italic = true,
                    ),
                ),
            )

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs[0].runs.size shouldBe 2
        }

        @Test
        fun `runs differing in spoiler are not merged`() {
            // ----- Arrange -----
            val doc = document(
                paragraph(
                    ReviewRun("Visible"),
                    ReviewRun(
                        "Hidden",
                        spoiler = true,
                    ),
                ),
            )

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs[0].runs.size shouldBe 2
        }

        @Test
        fun `empty runs are dropped before merging`() {
            // ----- Arrange -----
            val doc = document(
                paragraph(
                    ReviewRun("Hello"),
                    ReviewRun(""),
                    ReviewRun(" world"),
                ),
            )

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs[0].runs.size shouldBe 1
            result.paragraphs[0].runs[0].text shouldBe "Hello world"
        }

        @Test
        fun `canonical is idempotent - applying twice yields same result`() {
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
            val doc = document(
                paragraph(ReviewRun("First")),
                paragraph(ReviewRun("Second")),
            )

            // ----- Act -----
            val result = doc.canonical()

            // ----- Assert -----
            result.paragraphs.size shouldBe 2
            result.paragraphs[0].runs[0].text shouldBe "First"
            result.paragraphs[1].runs[0].text shouldBe "Second"
        }

        private fun goldenDocument() = document(
            paragraph(
                ReviewRun(
                    text = "Loved",
                    bold = true,
                ),
                ReviewRun(text = " it a ton. "),
            ),
            paragraph(
                ReviewRun(text = "Will be reading more by "),
                ReviewRun(
                    text = "Kylie Lee Baker",
                    italic = true,
                ),
                ReviewRun(text = "."),
            ),
            paragraph(
                ReviewRun(
                    text = "Test",
                    spoiler = true,
                ),
            ),
        )
    }
}
