package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.UserBook

class SaveBookVerdictUseCaseTest {
    private lateinit var updateBookRatingUseCase: UpdateBookRatingUseCase
    private lateinit var updateBookReviewUseCase: UpdateBookReviewUseCase
    private lateinit var useCase: SaveBookVerdictUseCase

    @BeforeEach
    fun setUp() {
        updateBookRatingUseCase = mockk()
        updateBookReviewUseCase = mockk()
        useCase = SaveBookVerdictUseCase(
            updateBookRatingUseCase = updateBookRatingUseCase,
            updateBookReviewUseCase = updateBookReviewUseCase,
        )
    }

    private fun stubBook(
        rating: Double?,
        reviewDocument: ReviewDocument?,
        reviewHasSpoilers: Boolean,
    ): Book {
        val userBook = mockk<UserBook>()
        val book = mockk<Book>()

        every {
            userBook.rating
        } returns rating

        every {
            userBook.reviewDocument
        } returns reviewDocument

        every {
            userBook.reviewHasSpoilers
        } returns reviewHasSpoilers

        every {
            book.userBook
        } returns userBook

        return book
    }

    @Nested
    inner class Invoke {
        @Test
        fun `skips the rating save when the draft rating is null`() = runTest {
            // ----- Arrange -----
            val book = stubBook(
                rating = 3.0,
                reviewDocument = ReviewDocument.EMPTY,
                reviewHasSpoilers = false,
            )

            // ----- Act -----
            val result = useCase(
                book = book,
                rating = null,
                review = ReviewDocument.EMPTY,
                hasSpoilers = false,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) {
                updateBookRatingUseCase(
                    book = any(),
                    rating = any(),
                )
            }
        }

        @Test
        fun `skips the rating save when the draft rating equals the existing rating`() = runTest {
            // ----- Arrange -----
            val book = stubBook(
                rating = 3.5,
                reviewDocument = ReviewDocument.EMPTY,
                reviewHasSpoilers = false,
            )

            // ----- Act -----
            val result = useCase(
                book = book,
                rating = 3.5,
                review = ReviewDocument.EMPTY,
                hasSpoilers = false,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) {
                updateBookRatingUseCase(
                    book = any(),
                    rating = any(),
                )
            }
        }

        @Test
        fun `saves the rating when it differs from the existing rating and skips the unchanged review`() = runTest {
            // ----- Arrange -----
            val book = stubBook(
                rating = 3.0,
                reviewDocument = ReviewDocument.EMPTY,
                reviewHasSpoilers = false,
            )

            coEvery {
                updateBookRatingUseCase(
                    book = book,
                    rating = 4.0,
                )
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase(
                book = book,
                rating = 4.0,
                review = ReviewDocument.EMPTY,
                hasSpoilers = false,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) {
                updateBookRatingUseCase(
                    book = book,
                    rating = 4.0,
                )
            }
            coVerify(exactly = 0) {
                updateBookReviewUseCase(
                    book = any(),
                    review = any(),
                    hasSpoilers = any(),
                )
            }
        }

        @Test
        fun `propagates a rating save failure and never attempts the review save`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("rating update failed")
            val book = stubBook(
                rating = 3.0,
                reviewDocument = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Old text"))))),
                reviewHasSpoilers = false,
            )

            coEvery {
                updateBookRatingUseCase(
                    book = book,
                    rating = 4.0,
                )
            } returns Result.failure(expectedError)

            // ----- Act -----
            val result = useCase(
                book = book,
                rating = 4.0,
                review = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("New text"))))),
                hasSpoilers = false,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) {
                updateBookReviewUseCase(
                    book = any(),
                    review = any(),
                    hasSpoilers = any(),
                )
            }
        }

        @Test
        fun `skips the review save when the canonical content and hasSpoilers flag are unchanged`() = runTest {
            // ----- Arrange -----
            val existingReview = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Great book")))))
            val newReview = ReviewDocument(
                listOf(ReviewParagraph(listOf(ReviewRun("Great"), ReviewRun(" book")))),
            )
            val book = stubBook(
                rating = 3.5,
                reviewDocument = existingReview,
                reviewHasSpoilers = false,
            )

            // ----- Act -----
            val result = useCase(
                book = book,
                rating = 3.5,
                review = newReview,
                hasSpoilers = false,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) {
                updateBookReviewUseCase(
                    book = any(),
                    review = any(),
                    hasSpoilers = any(),
                )
            }
        }

        @Test
        fun `treats a blank new review as unchanged when the existing review is also blank`() = runTest {
            // ----- Arrange -----
            val existingReview = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("")))))
            val book = stubBook(
                rating = 3.5,
                reviewDocument = existingReview,
                reviewHasSpoilers = false,
            )

            // ----- Act -----
            val result = useCase(
                book = book,
                rating = 3.5,
                review = ReviewDocument.EMPTY,
                hasSpoilers = false,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) {
                updateBookReviewUseCase(
                    book = any(),
                    review = any(),
                    hasSpoilers = any(),
                )
            }
        }

        @Test
        fun `saves the review when the canonical content changed and skips the unchanged rating`() = runTest {
            // ----- Arrange -----
            val existingReview = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Old text")))))
            val newReview = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("New text")))))
            val book = stubBook(
                rating = 3.5,
                reviewDocument = existingReview,
                reviewHasSpoilers = false,
            )

            coEvery {
                updateBookReviewUseCase(
                    book = book,
                    review = newReview,
                    hasSpoilers = false,
                )
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase(
                book = book,
                rating = 3.5,
                review = newReview,
                hasSpoilers = false,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) {
                updateBookRatingUseCase(
                    book = any(),
                    rating = any(),
                )
            }
            coVerify(exactly = 1) {
                updateBookReviewUseCase(
                    book = book,
                    review = newReview,
                    hasSpoilers = false,
                )
            }
        }

        @Test
        fun `saves the review when only the hasSpoilers flag changed while content stayed the same`() = runTest {
            // ----- Arrange -----
            val review = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Same text")))))
            val book = stubBook(
                rating = 3.5,
                reviewDocument = review,
                reviewHasSpoilers = false,
            )

            coEvery {
                updateBookReviewUseCase(
                    book = book,
                    review = review,
                    hasSpoilers = true,
                )
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase(
                book = book,
                rating = 3.5,
                review = review,
                hasSpoilers = true,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) {
                updateBookReviewUseCase(
                    book = book,
                    review = review,
                    hasSpoilers = true,
                )
            }
        }

        @Test
        fun `propagates a review save failure`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("review update failed")
            val existingReview = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Old text")))))
            val newReview = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("New text")))))
            val book = stubBook(
                rating = 3.5,
                reviewDocument = existingReview,
                reviewHasSpoilers = false,
            )

            coEvery {
                updateBookReviewUseCase(
                    book = book,
                    review = newReview,
                    hasSpoilers = false,
                )
            } returns Result.failure(expectedError)

            // ----- Act -----
            val result = useCase(
                book = book,
                rating = 3.5,
                review = newReview,
                hasSpoilers = false,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `saves both rating and review when both changed`() = runTest {
            // ----- Arrange -----
            val existingReview = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Old text")))))
            val newReview = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("New text")))))
            val book = stubBook(
                rating = 3.0,
                reviewDocument = existingReview,
                reviewHasSpoilers = false,
            )

            coEvery {
                updateBookRatingUseCase(
                    book = book,
                    rating = 4.0,
                )
            } returns Result.success(Unit)

            coEvery {
                updateBookReviewUseCase(
                    book = book,
                    review = newReview,
                    hasSpoilers = true,
                )
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase(
                book = book,
                rating = 4.0,
                review = newReview,
                hasSpoilers = true,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) {
                updateBookRatingUseCase(
                    book = book,
                    rating = 4.0,
                )
            }
            coVerify(exactly = 1) {
                updateBookReviewUseCase(
                    book = book,
                    review = newReview,
                    hasSpoilers = true,
                )
            }
        }
    }
}
