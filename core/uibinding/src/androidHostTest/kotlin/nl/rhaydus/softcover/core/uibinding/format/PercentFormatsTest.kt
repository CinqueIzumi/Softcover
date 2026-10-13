package nl.rhaydus.softcover.core.uibinding.format

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PercentFormatsTest {
    @Nested
    inner class PercentLabel {
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

        @Test
        fun `a fraction above one is capped at 100 percent`() {
            // ----- Arrange -----
            val fraction = 1.5f

            // ----- Act -----
            val result = percentLabel(fraction)

            // ----- Assert -----
            result shouldBe "100%"
        }

        @Test
        fun `a negative fraction is floored at 0 percent`() {
            // ----- Arrange -----
            val fraction = -0.2f

            // ----- Act -----
            val result = percentLabel(fraction)

            // ----- Assert -----
            result shouldBe "0%"
        }
    }
}
