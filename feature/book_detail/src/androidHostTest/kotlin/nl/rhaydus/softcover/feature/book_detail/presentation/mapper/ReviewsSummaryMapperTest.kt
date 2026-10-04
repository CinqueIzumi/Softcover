package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.Book
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReviewsSummaryMapperTest {
    private fun stubBook(
        rating: Double,
        ratingsCount: Int,
    ): Book = mockk {
        every {
            this@mockk.rating
        } returns rating

        every {
            this@mockk.ratingsCount
        } returns ratingsCount
    }

    @Nested
    inner class ToReviewsSummary {
        @Test
        fun `states the average to one decimal and the ratings count`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 4.2,
                ratingsCount = 87,
            )

            // ----- Act -----
            val result = book.toReviewsSummary()

            // ----- Assert -----
            result shouldBe "4.2 across 87 ratings"
        }

        @Test
        fun `rounds the average to one decimal`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 3.96,
                ratingsCount = 10,
            )

            // ----- Act -----
            val result = book.toReviewsSummary()

            // ----- Assert -----
            result shouldBe "4.0 across 10 ratings"
        }

        @Test
        fun `groups the thousands of a large ratings count`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 4.2,
                ratingsCount = 1204,
            )

            // ----- Act -----
            val result = book.toReviewsSummary()

            // ----- Assert -----
            result shouldBe "4.2 across 1,204 ratings"
        }

        @Test
        fun `is null when the book has no ratings`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                ratingsCount = 0,
            )

            // ----- Act -----
            val result = book.toReviewsSummary()

            // ----- Assert -----
            result shouldBe null
        }
    }
}
