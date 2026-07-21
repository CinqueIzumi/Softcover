package nl.rhaydus.softcover.core.designsystem.presentation.component

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.canonical

class ReviewRichTextTest {
    private fun mark(
        start: Int,
        end: Int,
        type: ReviewMarkType,
    ) =
        ReviewMark(
            start = start,
            end = end,
            type = type,
        )

    private fun bold(
        start: Int,
        end: Int,
    ) = mark(
        start,
        end,
        ReviewMarkType.BOLD,
    )

    private fun italic(
        start: Int,
        end: Int,
    ) = mark(
        start,
        end,
        ReviewMarkType.ITALIC,
    )

    private fun spoiler(
        start: Int,
        end: Int,
    ) = mark(
        start,
        end,
        ReviewMarkType.SPOILER,
    )

    @Nested
    inner class NormalizeMarks {
        @Test
        fun `drops empty marks where start equals end`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 3,
                end = 3,
            ),)

            // ----- Act -----
            val result = normalizeMarks(marks = marks)

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `drops marks where start is greater than end`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 5,
                end = 2,
            ),)

            // ----- Act -----
            val result = normalizeMarks(marks = marks)

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `preserves a valid non-overlapping mark`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 0,
                end = 4,
            ),)

            // ----- Act -----
            val result = normalizeMarks(marks = marks)

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 0,
                end = 4,
            ),)
        }

        @Test
        fun `merges two overlapping marks of the same type`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 0,
                end = 5,
            ), bold(
                start = 3,
                end = 8,
            ),)

            // ----- Act -----
            val result = normalizeMarks(marks = marks)

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 0,
                end = 8,
            ),)
        }

        @Test
        fun `merges two touching marks of the same type`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 0,
                end = 4,
            ), bold(
                start = 4,
                end = 8,
            ),)

            // ----- Act -----
            val result = normalizeMarks(marks = marks)

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 0,
                end = 8,
            ),)
        }

        @Test
        fun `leaves different-type marks independent`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 0,
                end = 4,
            ), italic(
                start = 0,
                end = 4,
            ),)

            // ----- Act -----
            val result = normalizeMarks(marks = marks)

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 0,
                end = 4,
            ), italic(
                start = 0,
                end = 4,
            ),)
        }

        @Test
        fun `result is sorted by start then type`() {
            // ----- Arrange -----
            val marks = listOf(
                italic(
                    start = 0,
                    end = 2,
                ),
                bold(
                    start = 0,
                    end = 2,
                ),
                spoiler(
                    start = 5,
                    end = 9,
                ),
            )

            // ----- Act -----
            val result = normalizeMarks(marks = marks)

            // ----- Assert -----
            result shouldBe listOf(
                bold(
                    start = 0,
                    end = 2,
                ),
                italic(
                    start = 0,
                    end = 2,
                ),
                spoiler(
                    start = 5,
                    end = 9,
                ),
            )
        }

        @Test
        fun `merges three overlapping same-type marks into one`() {
            // ----- Arrange -----
            val marks = listOf(
                bold(
                    start = 0,
                    end = 3,
                ),
                bold(
                    start = 2,
                    end = 6,
                ),
                bold(
                    start = 5,
                    end = 10,
                ),
            )

            // ----- Act -----
            val result = normalizeMarks(marks = marks)

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 0,
                end = 10,
            ),)
        }

        @Test
        fun `returns empty list when input is empty`() {
            // ----- Arrange & Act -----
            val result = normalizeMarks(marks = emptyList())

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class ApplyEditToMarks {
        @Test
        fun `returns normalized marks unchanged when text is not changed`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 2,
                end = 6,
            ),)

            // ----- Act -----
            val result = applyEditToMarks(
                marks = marks,
                oldText = "hello world",
                newText = "hello world",
            )

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 2,
                end = 6,
            ),)
        }

        @Test
        fun `shifts mark right when insert is before mark start`() {
            // ----- Arrange -----
            // "hello world" -> "XYZhello world": insert 3 chars at position 0
            val marks = listOf(bold(
                start = 6,
                end = 11,
            ),)

            // ----- Act -----
            val result = applyEditToMarks(
                marks = marks,
                oldText = "hello world",
                newText = "XYZhello world",
            )

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 9,
                end = 14,
            ),)
        }

        @Test
        fun `does not grow mark when insert is exactly at mark end`() {
            // ----- Arrange -----
            // "hello" -> "helloXYZ": insert 3 chars at position 5, mark covers [0,5)
            val marks = listOf(bold(
                start = 0,
                end = 5,
            ),)

            // ----- Act -----
            val result = applyEditToMarks(
                marks = marks,
                oldText = "hello",
                newText = "helloXYZ",
            )

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 0,
                end = 5,
            ),)
        }

        @Test
        fun `grows mark when insert is strictly inside it`() {
            // ----- Arrange -----
            // "hello" -> "helXYZlo": insert 3 chars at position 3 (strictly inside [0,5))
            val marks = listOf(bold(
                start = 0,
                end = 5,
            ),)

            // ----- Act -----
            val result = applyEditToMarks(
                marks = marks,
                oldText = "hello",
                newText = "helXYZlo",
            )

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 0,
                end = 8,
            ),)
        }

        @Test
        fun `shrinks mark when delete is fully inside it`() {
            // ----- Arrange -----
            // "hello world" -> "helloorld": delete 2 chars at positions [5, 7)
            val marks = listOf(bold(
                start = 0,
                end = 11,
            ),)

            // ----- Act -----
            val result = applyEditToMarks(
                marks = marks,
                oldText = "hello world",
                newText = "helloorld",
            )

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 0,
                end = 9,
            ),)
        }

        @Test
        fun `clips mark when delete spans its start`() {
            // ----- Arrange -----
            // "hello world" -> "world": delete first 6 chars [0,6), mark was [3,9)
            val marks = listOf(bold(
                start = 3,
                end = 9,
            ),)

            // ----- Act -----
            val result = applyEditToMarks(
                marks = marks,
                oldText = "hello world",
                newText = "world",
            )

            // ----- Assert -----
            // start was inside deleted span -> collapses to 0; end shifts by -6
            result shouldBe listOf(bold(
                start = 0,
                end = 3,
            ),)
        }

        @Test
        fun `removes mark when delete covers it entirely`() {
            // ----- Arrange -----
            // "hello world" -> "hd": delete "ello worl" [1,10), mark was [2,8)
            val marks = listOf(bold(
                start = 2,
                end = 8,
            ),)

            // ----- Act -----
            val result = applyEditToMarks(
                marks = marks,
                oldText = "hello world",
                newText = "hd",
            )

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `grows mark when insert is at exactly the mark start — start stays, end shifts`() {
            // ----- Arrange -----
            // "abcdefgh" -> "abcXXdefgh": insert 2 chars at position 3 (== mark start)
            val marks = listOf(bold(
                start = 3,
                end = 7,
            ),)

            // ----- Act -----
            val result = applyEditToMarks(
                marks = marks,
                oldText = "abcdefgh",
                newText = "abcXXdefgh",
            )

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 3,
                end = 9,
            ),)
        }

        @Test
        fun `preserves mark when replace is strictly inside it`() {
            // ----- Arrange -----
            // "hello" -> "hXo": replace "ell" [1,4) with "X", mark covers [0,5)
            val marks = listOf(bold(
                start = 0,
                end = 5,
            ),)

            // ----- Act -----
            val result = applyEditToMarks(
                marks = marks,
                oldText = "hello",
                newText = "hXo",
            )

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 0,
                end = 3,
            ),)
        }
    }

    @Nested
    inner class IsRangeMarked {
        @Test
        fun `returns true when mark exactly covers selection`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 2,
                end = 7,
            ),)

            // ----- Act & Assert -----
            isRangeMarked(
                marks = marks,
                start = 2,
                end = 7,
                type = ReviewMarkType.BOLD,
            ) shouldBe true
        }

        @Test
        fun `returns true when mark is a superset of selection`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 0,
                end = 10,
            ),)

            // ----- Act & Assert -----
            isRangeMarked(
                marks = marks,
                start = 3,
                end = 7,
                type = ReviewMarkType.BOLD,
            ) shouldBe true
        }

        @Test
        fun `returns false for partial coverage`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 0,
                end = 5,
            ),)

            // ----- Act & Assert -----
            isRangeMarked(
                marks = marks,
                start = 3,
                end = 8,
                type = ReviewMarkType.BOLD,
            ) shouldBe false
        }

        @Test
        fun `returns false for empty range`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 0,
                end = 10,
            ),)

            // ----- Act & Assert -----
            isRangeMarked(
                marks = marks,
                start = 5,
                end = 5,
                type = ReviewMarkType.BOLD,
            ) shouldBe false
        }

        @Test
        fun `returns false when type does not match`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 0,
                end = 5,
            ),)

            // ----- Act & Assert -----
            isRangeMarked(
                marks = marks,
                start = 0,
                end = 5,
                type = ReviewMarkType.ITALIC,
            ) shouldBe false
        }

        @Test
        fun `returns false when marks list is empty`() {
            // ----- Act & Assert -----
            isRangeMarked(
                marks = emptyList(),
                start = 0,
                end = 5,
                type = ReviewMarkType.BOLD,
            ) shouldBe false
        }
    }

    @Nested
    inner class ToggleMark {
        @Test
        fun `adds mark when selection is fully unmarked`() {
            // ----- Arrange -----
            val marks = emptyList<ReviewMark>()

            // ----- Act -----
            val result = toggleMark(
                marks = marks,
                start = 2,
                end = 6,
                type = ReviewMarkType.BOLD,
            )

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 2,
                end = 6,
            ),)
        }

        @Test
        fun `removes mark when selection is fully marked`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 0,
                end = 10,
            ),)

            // ----- Act -----
            val result = toggleMark(
                marks = marks,
                start = 2,
                end = 7,
                type = ReviewMarkType.BOLD,
            )

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 0,
                end = 2,
            ), bold(
                start = 7,
                end = 10,
            ),)
        }

        @Test
        fun `removes only the selected portion when selection matches mark exactly`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 3,
                end = 8,
            ),)

            // ----- Act -----
            val result = toggleMark(
                marks = marks,
                start = 3,
                end = 8,
                type = ReviewMarkType.BOLD,
            )

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `is a no-op for collapsed selection`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 0,
                end = 5,
            ),)

            // ----- Act -----
            val result = toggleMark(
                marks = marks,
                start = 3,
                end = 3,
                type = ReviewMarkType.BOLD,
            )

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 0,
                end = 5,
            ),)
        }

        @Test
        fun `adds mark over partial coverage`() {
            // ----- Arrange -----
            // Bold covers [0,4), selection is [2,7) — not fully marked so adds
            val marks = listOf(bold(
                start = 0,
                end = 4,
            ),)

            // ----- Act -----
            val result = toggleMark(
                marks = marks,
                start = 2,
                end = 7,
                type = ReviewMarkType.BOLD,
            )

            // ----- Assert -----
            result shouldBe listOf(bold(
                start = 0,
                end = 7,
            ),)
        }

        @Test
        fun `leaves other-type marks untouched when removing`() {
            // ----- Arrange -----
            val marks = listOf(bold(
                start = 0,
                end = 10,
            ), italic(
                start = 2,
                end = 6,
            ),)

            // ----- Act -----
            val result = toggleMark(
                marks = marks,
                start = 0,
                end = 10,
                type = ReviewMarkType.BOLD,
            )

            // ----- Assert -----
            result shouldBe listOf(italic(
                start = 2,
                end = 6,
            ),)
        }
    }

    @Nested
    inner class RoundTrip {
        @Test
        fun `documentToEditorBuffer then editorBufferToDocument returns canonical-equal document`() {
            // ----- Arrange -----
            val document = ReviewDocument(
                paragraphs = listOf(
                    ReviewParagraph(
                        runs = listOf(
                            ReviewRun(
                                text = "Hello ",
                                bold = false,
                                italic = false,
                                spoiler = false,
                            ),
                            ReviewRun(
                                text = "world",
                                bold = true,
                                italic = false,
                                spoiler = false,
                            ),
                        ),
                    ),
                    ReviewParagraph(runs = emptyList()),
                    ReviewParagraph(
                        runs = listOf(
                            ReviewRun(
                                text = "italic",
                                bold = false,
                                italic = true,
                                spoiler = false,
                            ),
                            ReviewRun(
                                text = " spoiler",
                                bold = false,
                                italic = false,
                                spoiler = true,
                            ),
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val buffer = documentToEditorBuffer(document = document)
            val recovered = editorBufferToDocument(
                text = buffer.text,
                marks = buffer.marks,
            )

            // ----- Assert -----
            recovered shouldBe document.canonical()
        }

        @Test
        fun `editorBufferToDocument with known text and marks returns expected document`() {
            // ----- Arrange -----
            // text = "boldnormal\nsecond"
            // marks: bold [0,4), spoiler [11,17)
            val text = "boldnormal\nsecond"
            val marks = listOf(
                bold(
                    start = 0,
                    end = 4,
                ),
                spoiler(
                    start = 11,
                    end = 17,
                ),
            )

            // ----- Act -----
            val result = editorBufferToDocument(
                text = text,
                marks = marks,
            )

            // ----- Assert -----
            val expected = ReviewDocument(
                paragraphs = listOf(
                    ReviewParagraph(
                        runs = listOf(
                            ReviewRun(
                                text = "bold",
                                bold = true,
                            ),
                            ReviewRun(text = "normal"),
                        ),
                    ),
                    ReviewParagraph(
                        runs = listOf(
                            ReviewRun(
                                text = "second",
                                spoiler = true,
                            ),
                        ),
                    ),
                ),
            )

            result shouldBe expected
        }

        @Test
        fun `empty line in text becomes paragraph with no runs`() {
            // ----- Arrange -----
            val text = "before\n\nafter"
            val marks = emptyList<ReviewMark>()

            // ----- Act -----
            val result = editorBufferToDocument(
                text = text,
                marks = marks,
            )

            // ----- Assert -----
            result.paragraphs.size shouldBe 3
            result.paragraphs[1].runs shouldBe emptyList()
        }

        @Test
        fun `document with empty paragraph survives round-trip`() {
            // ----- Arrange -----
            val document = ReviewDocument(
                paragraphs = listOf(
                    ReviewParagraph(runs = listOf(ReviewRun(text = "first"))),
                    ReviewParagraph(runs = emptyList()),
                    ReviewParagraph(runs = listOf(ReviewRun(text = "third"))),
                ),
            )

            // ----- Act -----
            val buffer = documentToEditorBuffer(document = document)
            val recovered = editorBufferToDocument(
                text = buffer.text,
                marks = buffer.marks,
            )

            // ----- Assert -----
            recovered shouldBe document.canonical()
        }

        @Test
        fun `run with all three marks survives round-trip`() {
            // ----- Arrange -----
            val document = ReviewDocument(
                paragraphs = listOf(
                    ReviewParagraph(
                        runs = listOf(
                            ReviewRun(
                                text = "secret",
                                bold = true,
                                italic = true,
                                spoiler = true,
                            ),
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val buffer = documentToEditorBuffer(document = document)
            val recovered = editorBufferToDocument(
                text = buffer.text,
                marks = buffer.marks,
            )

            // ----- Assert -----
            recovered shouldBe document.canonical()
        }

        @Test
        fun `documentToEditorBuffer splits multi-paragraph doc with newline separator`() {
            // ----- Arrange -----
            val document = ReviewDocument(
                paragraphs = listOf(
                    ReviewParagraph(runs = listOf(ReviewRun(text = "line one"))),
                    ReviewParagraph(runs = listOf(ReviewRun(text = "line two"))),
                ),
            )

            // ----- Act -----
            val buffer = documentToEditorBuffer(document = document)

            // ----- Assert -----
            buffer.text shouldBe "line one\nline two"
        }
    }
}
