package nl.rhaydus.softcover.core.component.verdict

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VerdictScoreOfTest {
    @Nested
    inner class VerdictScoreOf {
        @Test
        fun `doubles a whole star rating`() {
            // ----- Arrange -----
            val rating = 3.0

            // ----- Act -----
            val result = verdictScoreOf(rating = rating)

            // ----- Assert -----
            result shouldBe 6
        }

        @Test
        fun `doubles a half star rating to an odd score`() {
            // ----- Arrange -----
            val rating = 4.5

            // ----- Act -----
            val result = verdictScoreOf(rating = rating)

            // ----- Assert -----
            result shouldBe 9
        }

        @Test
        fun `rounds a quarter star rating to the nearest score`() {
            // ----- Arrange -----
            val rating = 4.25

            // ----- Act -----
            val result = verdictScoreOf(rating = rating)

            // ----- Assert -----
            result shouldBe 9
        }

        @Test
        fun `is null when there is no rating`() {
            // ----- Arrange -----
            val rating: Double? = null

            // ----- Act -----
            val result = verdictScoreOf(rating = rating)

            // ----- Assert -----
            result shouldBe null
        }
    }
}
