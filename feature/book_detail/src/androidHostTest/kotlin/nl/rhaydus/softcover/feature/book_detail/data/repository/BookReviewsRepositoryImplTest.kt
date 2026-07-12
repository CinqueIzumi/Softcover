package nl.rhaydus.softcover.feature.book_detail.data.repository

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.feature.book_detail.data.datasource.BookReviewsRemoteDataSource
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReviewer

class BookReviewsRepositoryImplTest {
    private lateinit var bookReviewsRemoteDataSource: BookReviewsRemoteDataSource
    private lateinit var repository: BookReviewsRepositoryImpl

    @BeforeEach
    fun setUp() {
        bookReviewsRemoteDataSource = mockk()
        repository = BookReviewsRepositoryImpl(
            bookReviewsRemoteDataSource = bookReviewsRemoteDataSource,
        )
    }

    private fun stubReview(id: Int = 1): BookReview = BookReview(
        id = id,
        reviewDocument = ReviewDocument(
            paragraphs = listOf(ReviewParagraph(runs = listOf(ReviewRun(text = "Great read")))),
        ),
        hasSpoilers = false,
        rating = 4.0,
        reviewedAt = "2024-01-01",
        likesCount = 5,
        reviewer = BookReviewer(
            id = 10,
            username = "alice",
            name = "Alice",
            avatarUrl = null,
        ),
    )

    @Nested
    inner class GetTopReviewsForBook {
        @Test
        fun `delegates to remote data source with the given bookId`() = runTest {
            // ----- Arrange -----
            val bookId = 42

            coEvery {
                bookReviewsRemoteDataSource.getTopReviewsForBook(bookId = bookId)
            } returns emptyList()

            // ----- Act -----
            repository.getTopReviewsForBook(bookId = bookId)

            // ----- Assert -----
            coVerify {
                bookReviewsRemoteDataSource.getTopReviewsForBook(bookId = bookId)
            }
        }

        @Test
        fun `returns the list returned by the remote data source`() = runTest {
            // ----- Arrange -----
            val bookId = 7
            val expectedReviews = listOf(stubReview(id = 1), stubReview(id = 2))

            coEvery {
                bookReviewsRemoteDataSource.getTopReviewsForBook(bookId = bookId)
            } returns expectedReviews

            // ----- Act -----
            val result = repository.getTopReviewsForBook(bookId = bookId)

            // ----- Assert -----
            result shouldBe expectedReviews
        }

        @Test
        fun `returns empty list when remote data source returns no reviews`() = runTest {
            // ----- Arrange -----
            val bookId = 99

            coEvery {
                bookReviewsRemoteDataSource.getTopReviewsForBook(bookId = bookId)
            } returns emptyList()

            // ----- Act -----
            val result = repository.getTopReviewsForBook(bookId = bookId)

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }
}
