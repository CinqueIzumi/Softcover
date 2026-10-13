package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LensToggleModelTest {
    @Nested
    inner class LensToggleFor {
        @Test
        fun `offers both lenses with the book first`() {
            // ----- Arrange -----
            val selectedLens = BookDetailLens.THE_BOOK
            val yoursEnabled = true

            // ----- Act -----
            val result = lensToggleFor(
                selectedLens = selectedLens,
                yoursEnabled = yoursEnabled,
            )

            // ----- Assert -----
            result.options.map { it.key } shouldBe listOf(
                BookDetailLens.THE_BOOK,
                BookDetailLens.YOURS,
            )
        }

        @Test
        fun `enables Yours when the book has a user copy`() {
            // ----- Arrange -----
            val selectedLens = BookDetailLens.THE_BOOK
            val yoursEnabled = true

            // ----- Act -----
            val result = lensToggleFor(
                selectedLens = selectedLens,
                yoursEnabled = yoursEnabled,
            )

            // ----- Assert -----
            result.options.first { it.key == BookDetailLens.YOURS }.enabled shouldBe true
        }

        @Test
        fun `disables Yours when the book has no user copy`() {
            // ----- Arrange -----
            val selectedLens = BookDetailLens.THE_BOOK
            val yoursEnabled = false

            // ----- Act -----
            val result = lensToggleFor(
                selectedLens = selectedLens,
                yoursEnabled = yoursEnabled,
            )

            // ----- Assert -----
            result.options.first { it.key == BookDetailLens.YOURS }.enabled shouldBe false
        }

        @Test
        fun `keeps The Book enabled whatever the user copy state`() {
            // ----- Arrange -----
            val selectedLens = BookDetailLens.THE_BOOK
            val yoursEnabled = false

            // ----- Act -----
            val result = lensToggleFor(
                selectedLens = selectedLens,
                yoursEnabled = yoursEnabled,
            )

            // ----- Assert -----
            result.options.first { it.key == BookDetailLens.THE_BOOK }.enabled shouldBe true
        }
    }
}
