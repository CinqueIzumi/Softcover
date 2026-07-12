package nl.rhaydus.softcover.core.connectivity.data.sync

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.offlinesync.ReplayOutcome
import nl.rhaydus.softcover.core.book.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.core.database.mapper.toJson
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun

class UserBookWriteReplayTest {
    private lateinit var booksRemoteDataSource: BooksRemoteDataSource
    private lateinit var replay: UserBookWriteReplay

    @BeforeEach
    fun setUp() {
        booksRemoteDataSource = mockk()
        replay = UserBookWriteReplay(booksRemoteDataSource = booksRemoteDataSource)
    }

    private fun pendingUserBookWrite(
        kind: PendingUserBookWriteKind,
        userBookId: Int = 10,
        userBookReadId: Int = 100,
        bookId: Int = 20,
        editionId: Int? = 200,
        progressPages: Int? = 50,
        progressSeconds: Int? = 3600,
        startedAt: String? = "2026-01-01",
        finishedAt: String? = null,
        rating: Double? = null,
        reviewSlateJson: String? = null,
        reviewHasSpoilers: Boolean? = null,
        enqueuedAt: String = "2026-05-04T12:34:56Z",
    ) = PendingUserBookWrite(
        kind = kind,
        userBookId = userBookId,
        userBookReadId = userBookReadId,
        bookId = bookId,
        editionId = editionId,
        progressPages = progressPages,
        progressSeconds = progressSeconds,
        startedAt = startedAt,
        finishedAt = finishedAt,
        rating = rating,
        reviewSlateJson = reviewSlateJson,
        reviewHasSpoilers = reviewHasSpoilers,
        enqueuedAt = enqueuedAt,
    )

    @Nested
    inner class UpdateProgress {
        @Test
        fun `calls replayUpdateBookProgress with the payload's fields and returns SYNCED`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookReadId = 100,
                editionId = 200,
                progressPages = 50,
                progressSeconds = 3600,
                startedAt = "2026-01-01",
                finishedAt = null,
            )

            coJustRun {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = 100,
                    editionId = 200,
                    progressPages = 50,
                    progressSeconds = 3600,
                    startedAt = "2026-01-01",
                    finishedAt = null,
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = 100,
                    editionId = 200,
                    progressPages = 50,
                    progressSeconds = 3600,
                    startedAt = "2026-01-01",
                    finishedAt = null,
                )
            }
        }

        @Test
        fun `propagates an exception thrown by the remote data source`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(kind = PendingUserBookWriteKind.UPDATE_PROGRESS)

            coEvery {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = any(),
                    editionId = any(),
                    progressPages = any(),
                    progressSeconds = any(),
                    startedAt = any(),
                    finishedAt = any(),
                )
            } throws RuntimeException("boom")

            // ----- Act & Assert -----
            shouldThrow<RuntimeException> { replay(payload = payload) }
        }
    }

    @Nested
    inner class MarkAsRead {
        @Test
        fun `calls replayMarkBookAsRead with the date-only prefix of enqueuedAt and returns SYNCED`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.MARK_AS_READ,
                bookId = 300,
                enqueuedAt = "2026-05-04T12:34:56Z",
            )

            coJustRun {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = 300,
                    userDate = "2026-05-04",
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = 300,
                    userDate = "2026-05-04",
                )
            }
        }
    }

    @Nested
    inner class UpdateRating {
        @Test
        fun `calls replayUpdateBookRating with the rating and returns SYNCED`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_RATING,
                userBookId = 30,
                rating = 4.5,
            )

            coJustRun {
                booksRemoteDataSource.replayUpdateBookRating(
                    userBookId = 30,
                    rating = 4.5,
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookRating(
                    userBookId = 30,
                    rating = 4.5,
                )
            }
        }

        @Test
        fun `returns DISCARDED and calls nothing when rating is null`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_RATING,
                rating = null,
            )

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.DISCARDED
            verify { booksRemoteDataSource wasNot Called }
        }
    }

    @Nested
    inner class UpdateReview {
        private val review = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Great book!")))))

        @Test
        fun `passes hasSpoilers and the date-only prefix of enqueuedAt for a valid review body`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_REVIEW,
                userBookId = 40,
                reviewSlateJson = review.toJson(),
                reviewHasSpoilers = true,
                enqueuedAt = "2026-05-30T10:15:30Z",
            )

            coJustRun {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = 40,
                    review = review,
                    hasSpoilers = true,
                    reviewedAt = "2026-05-30",
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = 40,
                    review = review,
                    hasSpoilers = true,
                    reviewedAt = "2026-05-30",
                )
            }
        }

        @Test
        fun `defaults hasSpoilers to false when reviewHasSpoilers is null`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_REVIEW,
                userBookId = 40,
                reviewSlateJson = review.toJson(),
                reviewHasSpoilers = null,
            )

            coJustRun {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = 40,
                    review = review,
                    hasSpoilers = false,
                    reviewedAt = any(),
                )
            }

            // ----- Act -----
            replay(payload = payload)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = 40,
                    review = review,
                    hasSpoilers = false,
                    reviewedAt = any(),
                )
            }
        }

        @Test
        fun `returns DISCARDED and calls nothing when reviewSlateJson does not parse`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_REVIEW,
                reviewSlateJson = "not valid json",
            )

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.DISCARDED
            verify { booksRemoteDataSource wasNot Called }
        }
    }
}
