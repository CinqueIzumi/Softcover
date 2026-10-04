package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReviewer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookReviewUiModelMapperTest {
    private fun review(
        rating: Double? = 4.0,
        reviewer: BookReviewer = BookReviewer(
            id = 5,
            username = "alice_reads",
            name = "Alice",
            avatarUrl = null,
        ),
        hasSpoilers: Boolean = false,
    ) = BookReview(
        id = 12,
        reviewDocument = ReviewDocument.EMPTY,
        hasSpoilers = hasSpoilers,
        rating = rating,
        reviewedAt = "2024-01-01T10:00:00",
        likesCount = 3,
        reviewer = reviewer,
    )

    @Nested
    inner class Rating {
        @Test
        fun `formats a whole rating with one decimal`() {
            // ----- Arrange -----
            val review = review(rating = 4.0)

            // ----- Act -----
            val model = review.toBookReviewUiModel()

            // ----- Assert -----
            model.rating shouldBe "4.0"
        }

        @Test
        fun `formats a fractional rating with one decimal`() {
            // ----- Arrange -----
            val review = review(rating = 3.5)

            // ----- Act -----
            val model = review.toBookReviewUiModel()

            // ----- Assert -----
            model.rating shouldBe "3.5"
        }

        @Test
        fun `maps a missing rating to null`() {
            // ----- Arrange -----
            val review = review(rating = null)

            // ----- Act -----
            val model = review.toBookReviewUiModel()

            // ----- Assert -----
            model.rating.shouldBeNull()
        }
    }

    @Nested
    inner class ReviewerName {
        @Test
        fun `uses the reviewer name when present`() {
            // ----- Arrange -----
            val review = review()

            // ----- Act -----
            val model = review.toBookReviewUiModel()

            // ----- Assert -----
            model.reviewerName shouldBe "Alice"
        }

        @Test
        fun `falls back to the username when the name is blank`() {
            // ----- Arrange -----
            val review = review(
                reviewer = BookReviewer(
                    id = 5,
                    username = "alice_reads",
                    name = "   ",
                    avatarUrl = null,
                ),
            )

            // ----- Act -----
            val model = review.toBookReviewUiModel()

            // ----- Assert -----
            model.reviewerName shouldBe "alice_reads"
        }

        @Test
        fun `falls back to the username when the name is null`() {
            // ----- Arrange -----
            val review = review(
                reviewer = BookReviewer(
                    id = 5,
                    username = "alice_reads",
                    name = null,
                    avatarUrl = null,
                ),
            )

            // ----- Act -----
            val model = review.toBookReviewUiModel()

            // ----- Assert -----
            model.reviewerName shouldBe "alice_reads"
        }
    }

    @Nested
    inner class Passthrough {
        @Test
        fun `maps a real review as loaded with its spoiler concealed`() {
            // ----- Arrange -----
            val review = review(hasSpoilers = true)

            // ----- Act -----
            val model = review.toBookReviewUiModel()

            // ----- Assert -----
            model.id shouldBe 12
            model.isLoading shouldBe false
            model.hasSpoilers shouldBe true
            model.spoilerRevealed shouldBe false
        }
    }
}
