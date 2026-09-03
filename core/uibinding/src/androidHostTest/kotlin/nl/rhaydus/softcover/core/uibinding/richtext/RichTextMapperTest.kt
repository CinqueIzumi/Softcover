package nl.rhaydus.softcover.core.uibinding.richtext

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.richtext.RichTextParagraph
import nl.rhaydus.softcover.core.component.richtext.RichTextRun
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RichTextMapperTest {
    @Nested
    inner class RoundTrip {
        @Test
        fun `domain to ui to domain returns a structurally equal ReviewDocument for every mark combination`() {
            // ----- Arrange -----
            val original = ReviewDocument(
                paragraphs = listOf(
                    ReviewParagraph(
                        runs = listOf(
                            ReviewRun(text = "plain"),
                            ReviewRun(
                                text = "bold only",
                                bold = true,
                            ),
                            ReviewRun(
                                text = "italic only",
                                italic = true,
                            ),
                        ),
                    ),
                    ReviewParagraph(
                        runs = listOf(
                            ReviewRun(
                                text = "spoiler only",
                                spoiler = true,
                            ),
                            ReviewRun(
                                text = "bold and italic",
                                bold = true,
                                italic = true,
                            ),
                            ReviewRun(
                                text = "everything",
                                bold = true,
                                italic = true,
                                spoiler = true,
                            ),
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val roundTripped = original.toRichTextUiModel().toReviewDocument()

            // ----- Assert -----
            roundTripped shouldBe original
        }

        @Test
        fun `ui to domain to ui returns a structurally equal RichTextUiModel for every mark combination`() {
            // ----- Arrange -----
            val original = RichTextUiModel(
                paragraphs = persistentListOf(
                    RichTextParagraph(
                        runs = persistentListOf(
                            RichTextRun(text = "plain"),
                            RichTextRun(
                                text = "bold only",
                                bold = true,
                            ),
                            RichTextRun(
                                text = "italic only",
                                italic = true,
                            ),
                        ),
                    ),
                    RichTextParagraph(
                        runs = persistentListOf(
                            RichTextRun(
                                text = "spoiler only",
                                spoiler = true,
                            ),
                            RichTextRun(
                                text = "bold and italic",
                                bold = true,
                                italic = true,
                            ),
                            RichTextRun(
                                text = "everything",
                                bold = true,
                                italic = true,
                                spoiler = true,
                            ),
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val roundTripped = original.toReviewDocument().toRichTextUiModel()

            // ----- Assert -----
            roundTripped shouldBe original
        }

        @Test
        fun `EMPTY round-trips in both directions`() {
            // ----- Act -----
            val domainRoundTripped = ReviewDocument.EMPTY.toRichTextUiModel().toReviewDocument()
            val uiRoundTripped = RichTextUiModel.EMPTY.toReviewDocument().toRichTextUiModel()

            // ----- Assert -----
            domainRoundTripped shouldBe ReviewDocument.EMPTY
            uiRoundTripped shouldBe RichTextUiModel.EMPTY
        }

        @Test
        fun `a paragraph with no runs survives both directions`() {
            // ----- Arrange -----
            val domainDocument = ReviewDocument(paragraphs = listOf(ReviewParagraph(runs = emptyList())))
            val uiModel = RichTextUiModel(
                paragraphs = persistentListOf(RichTextParagraph(runs = persistentListOf())),
            )

            // ----- Act -----
            val domainRoundTripped = domainDocument.toRichTextUiModel().toReviewDocument()
            val uiRoundTripped = uiModel.toReviewDocument().toRichTextUiModel()

            // ----- Assert -----
            domainRoundTripped shouldBe domainDocument
            uiRoundTripped shouldBe uiModel
        }
    }

    @Nested
    inner class FieldFidelity {
        @Test
        fun `bold italic and spoiler map independently to the ui model`() {
            // ----- Arrange -----
            val document = ReviewDocument(
                paragraphs = listOf(
                    ReviewParagraph(
                        runs = listOf(
                            ReviewRun(
                                text = "bold",
                                bold = true,
                            ),
                            ReviewRun(
                                text = "italic",
                                italic = true,
                            ),
                            ReviewRun(
                                text = "spoiler",
                                spoiler = true,
                            ),
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val runs = document.toRichTextUiModel().paragraphs.single().runs

            // ----- Assert -----
            runs[0] shouldBe RichTextRun(
                text = "bold",
                bold = true,
            )
            runs[1] shouldBe RichTextRun(
                text = "italic",
                italic = true,
            )
            runs[2] shouldBe RichTextRun(
                text = "spoiler",
                spoiler = true,
            )
        }

        @Test
        fun `bold italic and spoiler map independently to the domain document`() {
            // ----- Arrange -----
            val uiModel = RichTextUiModel(
                paragraphs = persistentListOf(
                    RichTextParagraph(
                        runs = persistentListOf(
                            RichTextRun(
                                text = "bold",
                                bold = true,
                            ),
                            RichTextRun(
                                text = "italic",
                                italic = true,
                            ),
                            RichTextRun(
                                text = "spoiler",
                                spoiler = true,
                            ),
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val runs = uiModel.toReviewDocument().paragraphs.single().runs

            // ----- Assert -----
            runs[0] shouldBe ReviewRun(
                text = "bold",
                bold = true,
            )
            runs[1] shouldBe ReviewRun(
                text = "italic",
                italic = true,
            )
            runs[2] shouldBe ReviewRun(
                text = "spoiler",
                spoiler = true,
            )
        }

        @Test
        fun `text is carried verbatim including empty strings and embedded newlines to the ui model`() {
            // ----- Arrange -----
            val document = ReviewDocument(
                paragraphs = listOf(
                    ReviewParagraph(
                        runs = listOf(
                            ReviewRun(text = ""),
                            ReviewRun(text = "line one\nline two"),
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val runs = document.toRichTextUiModel().paragraphs.single().runs

            // ----- Assert -----
            runs[0].text shouldBe ""
            runs[1].text shouldBe "line one\nline two"
        }

        @Test
        fun `text is carried verbatim including empty strings and embedded newlines to the domain document`() {
            // ----- Arrange -----
            val uiModel = RichTextUiModel(
                paragraphs = persistentListOf(
                    RichTextParagraph(
                        runs = persistentListOf(
                            RichTextRun(text = ""),
                            RichTextRun(text = "line one\nline two"),
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val runs = uiModel.toReviewDocument().paragraphs.single().runs

            // ----- Assert -----
            runs[0].text shouldBe ""
            runs[1].text shouldBe "line one\nline two"
        }
    }

    @Nested
    inner class Structure {
        @Test
        fun `paragraph order and run order are preserved from domain to ui`() {
            // ----- Arrange -----
            val document = ReviewDocument(
                paragraphs = listOf(
                    ReviewParagraph(
                        runs = listOf(
                            ReviewRun(text = "first paragraph, first run"),
                            ReviewRun(text = "first paragraph, second run"),
                        ),
                    ),
                    ReviewParagraph(
                        runs = listOf(
                            ReviewRun(text = "second paragraph, first run"),
                            ReviewRun(text = "second paragraph, second run"),
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val mapped = document.toRichTextUiModel()

            // ----- Assert -----
            mapped.paragraphs.map { paragraph -> paragraph.runs.map { it.text } } shouldBe listOf(
                listOf("first paragraph, first run", "first paragraph, second run"),
                listOf("second paragraph, first run", "second paragraph, second run"),
            )
        }

        @Test
        fun `paragraph order and run order are preserved from ui to domain`() {
            // ----- Arrange -----
            val uiModel = RichTextUiModel(
                paragraphs = persistentListOf(
                    RichTextParagraph(
                        runs = persistentListOf(
                            RichTextRun(text = "first paragraph, first run"),
                            RichTextRun(text = "first paragraph, second run"),
                        ),
                    ),
                    RichTextParagraph(
                        runs = persistentListOf(
                            RichTextRun(text = "second paragraph, first run"),
                            RichTextRun(text = "second paragraph, second run"),
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val mapped = uiModel.toReviewDocument()

            // ----- Assert -----
            mapped.paragraphs.map { paragraph -> paragraph.runs.map { it.text } } shouldBe listOf(
                listOf("first paragraph, first run", "first paragraph, second run"),
                listOf("second paragraph, first run", "second paragraph, second run"),
            )
        }
    }
}
