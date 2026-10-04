package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LensToggleModelTest {
    @Nested
    inner class LensToggleFor {
        @Test
        fun `offers both lenses with the book first`() {
            // ----- Arrange & Act -----
            val result = lensToggleFor(
                selectedLens = BookDetailLens.THE_BOOK,
                yoursEnabled = true,
            )

            // ----- Assert -----
            result.options.map { it.key } shouldBe listOf(
                BookDetailLens.THE_BOOK,
                BookDetailLens.YOURS,
            )
        }

        @Test
        fun `marks the given lens as selected`() {
            // ----- Arrange & Act -----
            val result = lensToggleFor(
                selectedLens = BookDetailLens.YOURS,
                yoursEnabled = true,
            )

            // ----- Assert -----
            result.selected shouldBe BookDetailLens.YOURS
        }

        @Test
        fun `enables Yours when the book has a user copy`() {
            // ----- Arrange & Act -----
            val result = lensToggleFor(
                selectedLens = BookDetailLens.THE_BOOK,
                yoursEnabled = true,
            )

            // ----- Assert -----
            result.options.first { it.key == BookDetailLens.YOURS }.enabled shouldBe true
        }

        @Test
        fun `disables Yours when the book has no user copy`() {
            // ----- Arrange & Act -----
            val result = lensToggleFor(
                selectedLens = BookDetailLens.THE_BOOK,
                yoursEnabled = false,
            )

            // ----- Assert -----
            result.options.first { it.key == BookDetailLens.YOURS }.enabled shouldBe false
        }

        @Test
        fun `keeps The Book enabled whatever the user copy state`() {
            // ----- Arrange & Act -----
            val result = lensToggleFor(
                selectedLens = BookDetailLens.THE_BOOK,
                yoursEnabled = false,
            )

            // ----- Assert -----
            result.options.first { it.key == BookDetailLens.THE_BOOK }.enabled shouldBe true
        }
    }
}
