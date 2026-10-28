package nl.rhaydus.softcover.core.profile.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LengthBandTest {
    @Nested
    inner class FromPages {
        @Test
        fun `249 pages is under 250`() {
            // ----- Arrange -----
            val pages = 249

            // ----- Act & Assert -----
            LengthBand.fromPages(pages) shouldBe LengthBand.UNDER_250
        }

        @Test
        fun `250 pages starts the middle band`() {
            // ----- Arrange -----
            val pages = 250

            // ----- Act & Assert -----
            LengthBand.fromPages(pages) shouldBe LengthBand.FROM_250_TO_450
        }

        @Test
        fun `450 pages ends the middle band`() {
            // ----- Arrange -----
            val pages = 450

            // ----- Act & Assert -----
            LengthBand.fromPages(pages) shouldBe LengthBand.FROM_250_TO_450
        }

        @Test
        fun `451 pages is over 450`() {
            // ----- Arrange -----
            val pages = 451

            // ----- Act & Assert -----
            LengthBand.fromPages(pages) shouldBe LengthBand.OVER_450
        }

        @Test
        fun `unknown, zero and negative lengths have no band`() {
            // ----- Arrange -----
            val lengths = listOf(null, 0, -5)

            // ----- Act -----
            val bands = lengths.map { LengthBand.fromPages(it) }

            // ----- Assert -----
            bands shouldBe listOf(null, null, null)
        }
    }
}
