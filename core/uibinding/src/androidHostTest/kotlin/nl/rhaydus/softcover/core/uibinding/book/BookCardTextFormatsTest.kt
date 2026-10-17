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
