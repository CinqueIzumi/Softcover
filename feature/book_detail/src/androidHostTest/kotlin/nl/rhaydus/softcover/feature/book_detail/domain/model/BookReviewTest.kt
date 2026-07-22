package nl.rhaydus.softcover.feature.book_detail.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.ReviewDocument

class BookReviewTest {
    private fun buildReview(reviewedAt: String?): BookReview = BookReview(
        id = 1,
        reviewDocument = ReviewDocument.EMPTY,
        hasSpoilers = false,
        rating = null,
        reviewedAt = reviewedAt,
        likesCount = 0,
        reviewer = BookReviewer(
            id = 1,
            username = "user",
            name = null,
            avatarUrl = null,
        ),
    )

    @Nested
    inner class GetReviewedMonthYear {
        @Test
        fun `returns null when reviewedAt is null`() {
            // ----- Arrange -----
            val review = buildReview(reviewedAt = null)

            // ----- Act -----
            val result = review.getReviewedMonthYear()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when reviewedAt is an unparseable string`() {
            // ----- Arrange -----
            val review = buildReview(reviewedAt = "not-a-date")

            // ----- Act -----
            val result = review.getReviewedMonthYear()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when reviewedAt is an empty string`() {
            // ----- Arrange -----
            val review = buildReview(reviewedAt = "")

            // ----- Act -----
            val result = review.getReviewedMonthYear()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `formats valid ISO-8601 date-time as abbreviated month and year`() {
            // ----- Arrange -----
            val review = buildReview(reviewedAt = "2024-05-15T10:23:45")

            // ----- Act -----
            val result = review.getReviewedMonthYear()

            // ----- Assert -----
            result shouldBe "May 2024"
        }

        @Test
        fun `formats a different valid ISO-8601 date-time as abbreviated month and year`() {
            // ----- Arrange -----
            val review = buildReview(reviewedAt = "2023-09-03T00:00:00")

            // ----- Act -----
            val result = review.getReviewedMonthYear()

            // ----- Assert -----
            result shouldBe "Sep 2023"
        }
    }
}
