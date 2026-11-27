package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MedianTest {
    @Nested
    inner class LongMedian {
        @Test
        fun `an odd count takes the middle value of the sorted list`() {
            // ----- Arrange -----
            val values = listOf(9_000L, 1_000L, 5_000L)

            // ----- Act -----
            val result = values.median()

            // ----- Assert -----
            result shouldBe 5_000L
        }

        @Test
        fun `an even count averages the middle two`() {
            // ----- Arrange -----
            val values = listOf(8_000L, 1_000L, 4_000L, 2_000L)

            // ----- Act -----
            val result = values.median()

            // ----- Assert -----
            result shouldBe 3_000L
        }

        @Test
        fun `an even count rounds half up`() {
            // ----- Arrange -----
            val values = listOf(1L, 2L)

            // ----- Act -----
            val result = values.median()

            // ----- Assert -----
            result shouldBe 2L
        }

        @Test
        fun `a single value is its own median`() {
            // ----- Arrange -----
            val values = listOf(12_500L)

            // ----- Act -----
            val result = values.median()

            // ----- Assert -----
            result shouldBe 12_500L
        }
    }
}
