package nl.rhaydus.softcover.feature.book_detail.domain.model

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
    inner class GetFormattedDate {

        @Test
        fun `returns null when reviewedAt is null`() {
            // ----- Arrange -----
            val review = buildReview(reviewedAt = null)

            // ----- Act -----
            val result = review.getFormattedDate(style = DateStyle.DAY_MONTH_YEAR)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when reviewedAt is an unparseable string`() {
            // ----- Arrange -----
            val review = buildReview(reviewedAt = "not-a-date")

            // ----- Act -----
            val result = review.getFormattedDate(style = DateStyle.DAY_MONTH_YEAR)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when reviewedAt is an empty string`() {
            // ----- Arrange -----
            val review = buildReview(reviewedAt = "")

            // ----- Act -----
            val result = review.getFormattedDate(style = DateStyle.DAY_MONTH_YEAR)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `formats valid ISO-8601 date-time with DAY_MONTH_YEAR style`() {
            // ----- Arrange -----
            val review = buildReview(reviewedAt = "2024-05-15T10:23:45")

            // ----- Act -----
            val result = review.getFormattedDate(style = DateStyle.DAY_MONTH_YEAR)

            // ----- Assert -----
            result shouldBe "15/05/2024"
        }

        @Test
        fun `formats valid ISO-8601 date-time with MONTH_DAY_YEAR style`() {
            // ----- Arrange -----
            val review = buildReview(reviewedAt = "2024-05-15T10:23:45")

            // ----- Act -----
            val result = review.getFormattedDate(
                style = nl.rhaydus.softcover.feature.settings.domain.model.DateStyle.MONTH_DAY_YEAR
            )

            // ----- Assert -----
            result shouldBe "05/15/2024"
        }

        @Test
        fun `formats valid ISO-8601 date-time with YEAR_MONTH_DAY style`() {
            // ----- Arrange -----
            val review = buildReview(reviewedAt = "2024-05-15T10:23:45")

            // ----- Act -----
            val result = review.getFormattedDate(
                style = nl.rhaydus.softcover.feature.settings.domain.model.DateStyle.YEAR_MONTH_DAY
            )

            // ----- Assert -----
            result shouldBe "2024/05/15"
        }
    }
}
