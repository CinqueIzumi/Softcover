package nl.rhaydus.softcover.core.profile.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RatingBandTest {
    @Nested
    inner class Classify {
        @Test
        fun `fewer than 15 total ratings is NEW_READER regardless of average or shape`() {
            // ----- Act & Assert -----
            RatingBand.classify(
                halfStarBuckets = List(9) { 0 },
                average = 4.8,
                totalRatings = 10,
            ) shouldBe RatingBand.NEW_READER
        }

        @Test
        fun `a bimodal distribution is POLARISED even at 20 total ratings`() {
            // ----- Arrange -----
            // low (1.0-2.0) = 5, mid (2.5-3.5) = 1, high (4.0-5.0) = 8, total = 20.
            // lowFraction 0.25 and highFraction 0.4 both clear the 0.2 wing threshold, and the
            // mid trough (0.05) is below both wings.
            val buckets = listOf(3, 1, 1, 0, 1, 0, 2, 3, 3)

            // ----- Act & Assert -----
            RatingBand.classify(
                halfStarBuckets = buckets,
                average = 3.0,
                totalRatings = 20,
            ) shouldBe RatingBand.POLARISED
        }

        @Test
        fun `POLARISED takes priority over SUPERFAN when the distribution is bimodal`() {
            // ----- Arrange -----
            // Same bimodal shape as above, but the average alone would qualify as SUPERFAN.
            val buckets = listOf(3, 1, 1, 0, 1, 0, 2, 3, 3)

            // ----- Act & Assert -----
            RatingBand.classify(
                halfStarBuckets = buckets,
                average = 4.7,
                totalRatings = 20,
            ) shouldBe RatingBand.POLARISED
        }

        @Test
        fun `average of 4_6 is SUPERFAN, not GENEROUS`() {
            // ----- Arrange -----
            // High-wing heavy but with an empty low wing, so this is not mistaken for POLARISED.
            val buckets = listOf(0, 0, 0, 0, 0, 0, 2, 8, 10)

            // ----- Act & Assert -----
            RatingBand.classify(
                halfStarBuckets = buckets,
                average = 4.6,
                totalRatings = 20,
            ) shouldBe RatingBand.SUPERFAN
        }

        @Test
        fun `a genuine superfan shape (empty low wing) is not misread as POLARISED`() {
            // ----- Arrange -----
            val buckets = listOf(0, 0, 0, 0, 0, 0, 2, 8, 10)

            // ----- Act & Assert -----
            RatingBand.classify(
                halfStarBuckets = buckets,
                average = 4.9,
                totalRatings = 20,
            ) shouldBe RatingBand.SUPERFAN
        }

        @Test
        fun `average just under 4_6 is not SUPERFAN`() {
            // ----- Arrange -----
            val buckets = listOf(0, 0, 0, 0, 0, 0, 2, 8, 10)

            // ----- Act & Assert -----
            RatingBand.classify(
                halfStarBuckets = buckets,
                average = 4.59,
                totalRatings = 20,
            ) shouldBe RatingBand.GENEROUS
        }

        @Test
        fun `average below 3_2 is EXACTING`() {
            // ----- Arrange -----
            // Concentrated in the low and mid bands only, empty high wing - not bimodal.
            val buckets = listOf(5, 5, 5, 0, 0, 0, 0, 0, 0)

            // ----- Act & Assert -----
            RatingBand.classify(
                halfStarBuckets = buckets,
                average = 3.0,
                totalRatings = 15,
            ) shouldBe RatingBand.EXACTING
        }

        @Test
        fun `average of 3_3 is the lower CENTRIST boundary`() {
            // ----- Arrange -----
            val buckets = listOf(0, 0, 0, 5, 5, 5, 0, 0, 0)

            // ----- Act & Assert -----
            RatingBand.classify(
                halfStarBuckets = buckets,
                average = 3.3,
                totalRatings = 15,
            ) shouldBe RatingBand.CENTRIST
        }

        @Test
        fun `average of 3_8 is the upper CENTRIST boundary`() {
            // ----- Arrange -----
            val buckets = listOf(0, 0, 0, 5, 5, 5, 0, 0, 0)

            // ----- Act & Assert -----
            RatingBand.classify(
                halfStarBuckets = buckets,
                average = 3.8,
                totalRatings = 15,
            ) shouldBe RatingBand.CENTRIST
        }

        @Test
        fun `average just below the CENTRIST range falls through to GENEROUS`() {
            // ----- Arrange -----
            val buckets = listOf(0, 0, 0, 5, 5, 5, 0, 0, 0)

            // ----- Act & Assert -----
            RatingBand.classify(
                halfStarBuckets = buckets,
                average = 3.29,
                totalRatings = 15,
            ) shouldBe RatingBand.GENEROUS
        }

        @Test
        fun `average just above the CENTRIST range falls through to GENEROUS`() {
            // ----- Arrange -----
            val buckets = listOf(0, 0, 0, 5, 5, 5, 0, 0, 0)

            // ----- Act & Assert -----
            RatingBand.classify(
                halfStarBuckets = buckets,
                average = 3.81,
                totalRatings = 15,
            ) shouldBe RatingBand.GENEROUS
        }

        @Test
        fun `a non-bimodal, non-extreme average defaults to GENEROUS`() {
            // ----- Arrange -----
            val buckets = listOf(0, 0, 2, 3, 5, 4, 3, 2, 1)

            // ----- Act & Assert -----
            RatingBand.classify(
                halfStarBuckets = buckets,
                average = 4.0,
                totalRatings = 20,
            ) shouldBe RatingBand.GENEROUS
        }
    }
}
