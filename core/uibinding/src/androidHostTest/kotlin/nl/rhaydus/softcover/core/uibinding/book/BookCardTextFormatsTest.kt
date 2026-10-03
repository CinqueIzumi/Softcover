package nl.rhaydus.softcover.core.uibinding.book

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookCardTextFormatsTest {
    @Nested
    inner class Byline {
        @Test
        fun `an author is prefixed with By`() {
            // ----- Arrange -----
            val author = "Frank Herbert"

            // ----- Act -----
            val result = byline(author)

            // ----- Assert -----
            result shouldBe "By Frank Herbert"
        }

        @Test
        fun `a null or blank author yields null`() {
            // ----- Arrange -----
            val inputs = listOf(null, "", "   ")

            // ----- Act -----
            val results = inputs.map { author -> byline(author) }

            // ----- Assert -----
            results shouldBe inputs.map { null }
        }
    }

    @Nested
    inner class PercentLabel {
        @Test
        fun `a fraction becomes a whole percent`() {
            // ----- Arrange -----
            val fraction = 0.42f

            // ----- Act -----
            val result = percentLabel(fraction)

            // ----- Assert -----
            result shouldBe "42%"
        }

        @Test
        fun `a fraction rounds to the nearest percent`() {
            // ----- Arrange -----
            val roundedUp = 0.426f
            val roundedDown = 0.424f

            // ----- Act -----
            val up = percentLabel(roundedUp)
            val down = percentLabel(roundedDown)

            // ----- Assert -----
            up shouldBe "43%"
            down shouldBe "42%"
        }

        @Test
        fun `the bounds read 0 and 100 percent`() {
            // ----- Arrange -----
            val lowerBound = 0f
            val upperBound = 1f

            // ----- Act -----
            val zero = percentLabel(lowerBound)
            val full = percentLabel(upperBound)

            // ----- Assert -----
            zero shouldBe "0%"
            full shouldBe "100%"
        }
    }

    @Nested
    inner class RatingPart {
        @Test
        fun `the label shows one decimal and the description reads out of 5`() {
            // ----- Arrange -----
            val rating = 4.26

            // ----- Act -----
            val result = ratingPart(rating)

            // ----- Assert -----
            result.label shouldBe "4.3"
            result.contentDescription shouldBe "Rated 4.3 out of 5"
        }
    }

    @Nested
    inner class SeriesEyebrow {
        @Test
        fun `a series is uppercased`() {
            // ----- Arrange -----
            val series = "Dune Chronicles #1"

            // ----- Act -----
            val result = seriesEyebrow(series)

            // ----- Assert -----
            result.shouldNotBeNull().text shouldBe "DUNE CHRONICLES #1"
        }

        @Test
        fun `a null or blank series yields null`() {
            // ----- Arrange -----
            val inputs = listOf(null, "", "  ")

            // ----- Act -----
            val results = inputs.map { series -> seriesEyebrow(series) }

            // ----- Assert -----
            results.forEach { result -> result.shouldBeNull() }
        }
    }
}
