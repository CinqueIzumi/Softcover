package nl.rhaydus.softcover.feature.book_detail.domain.usecase

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
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReviewer
import nl.rhaydus.softcover.feature.book_detail.domain.repository.BookReviewsRepository

class GetTopBookReviewsUseCaseTest {
    private lateinit var bookReviewsRepository: BookReviewsRepository
    private lateinit var useCase: GetTopBookReviewsUseCase

    @BeforeEach
    fun setUp() {
        bookReviewsRepository = mockk()
        useCase = GetTopBookReviewsUseCase(bookReviewsRepository = bookReviewsRepository)
    }

    private fun stubReview(id: Int = 1): BookReview = BookReview(
        id = id,
        reviewDocument = ReviewDocument(
            paragraphs = listOf(ReviewParagraph(runs = listOf(ReviewRun(text = "Nice book")))),
        ),
        hasSpoilers = false,
        rating = 3.5,
        reviewedAt = "2024-06-01",
        likesCount = 2,
        reviewer = BookReviewer(
            id = 20,
            username = "bob",
            name = "Bob",
            avatarUrl = null,
        ),
    )

    @Nested
    inner class Invoke {
        @Test
        fun `returns success with the list from the repository`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val expectedReviews = listOf(stubReview(id = 1), stubReview(id = 2))

            coEvery {
                bookReviewsRepository.getTopReviewsForBook(bookId = bookId)
            } returns expectedReviews

            // ----- Act -----
            val result = useCase(bookId = bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe expectedReviews
        }

        @Test
        fun `returns success with empty list when repository returns no reviews`() = runTest {
            // ----- Arrange -----
            val bookId = 7

            coEvery {
                bookReviewsRepository.getTopReviewsForBook(bookId = bookId)
            } returns emptyList()

            // ----- Act -----
            val result = useCase(bookId = bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe emptyList()
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val expectedError = RuntimeException("network failure")

            coEvery {
                bookReviewsRepository.getTopReviewsForBook(bookId = bookId)
            } throws expectedError

            // ----- Act -----
            val result = useCase(bookId = bookId)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `invokes repository with the given bookId`() = runTest {
            // ----- Arrange -----
            val bookId = 99

            coEvery {
                bookReviewsRepository.getTopReviewsForBook(bookId = bookId)
            } returns emptyList()

            // ----- Act -----
            useCase(bookId = bookId)

            // ----- Assert -----
            coVerify {
                bookReviewsRepository.getTopReviewsForBook(bookId = bookId)
            }
        }

        @Test
        fun `wraps the exception in the Result rather than propagating it`() = runTest {
            // ----- Arrange -----
            val bookId = 3

            coEvery {
                bookReviewsRepository.getTopReviewsForBook(bookId = bookId)
            } throws IllegalStateException("server error")

            // ----- Act -----
            val result = runCatching { useCase(bookId = bookId) }

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull()!!.isFailure shouldBe true
        }
    }
}
