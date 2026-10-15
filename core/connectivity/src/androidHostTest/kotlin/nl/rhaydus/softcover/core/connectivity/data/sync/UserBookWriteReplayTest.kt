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
import nl.rhaydus.offlinesync.ReplayOutcome
import nl.rhaydus.softcover.core.book.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.core.book.data.datasource.ProgressHistoryLocalDataSource
import nl.rhaydus.softcover.core.database.mapper.toJson
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.exception.ServerUnavailableException
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.util.localDateOf
import java.util.TimeZone
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserBookWriteReplayTest {
    private lateinit var booksRemoteDataSource: BooksRemoteDataSource
    private lateinit var progressHistoryLocalDataSource: ProgressHistoryLocalDataSource
    private lateinit var replay: UserBookWriteReplay
    private lateinit var originalZone: TimeZone

    @BeforeEach
    fun setUp() {
        originalZone = TimeZone.getDefault()
        booksRemoteDataSource = mockk()
        progressHistoryLocalDataSource = mockk(relaxed = true)
        replay = UserBookWriteReplay(
            booksRemoteDataSource = booksRemoteDataSource,
            progressHistoryLocalDataSource = progressHistoryLocalDataSource,
        )
    }

    @AfterEach
    fun tearDown() {
        TimeZone.setDefault(originalZone)
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
        actionAt: String? = null,
        progressHistoryId: Long? = null,
        statusCode: Int? = null,
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
        actionAt = actionAt,
        progressHistoryId = progressHistoryId,
        statusCode = statusCode,
    )

    private fun stubReplayProgress() {
        coJustRun {
            booksRemoteDataSource.replayUpdateBookProgress(
                userBookReadId = any(),
                editionId = any(),
                progressPages = any(),
                progressSeconds = any(),
                startedAt = any(),
                finishedAt = any(),
                actionAt = any(),
            )
        }
    }

    private fun stubReplayProgressThrowing(error: Throwable) {
        coEvery {
            booksRemoteDataSource.replayUpdateBookProgress(
                userBookReadId = any(),
                editionId = any(),
                progressPages = any(),
                progressSeconds = any(),
                startedAt = any(),
                finishedAt = any(),
                actionAt = any(),
            )
        } throws error
    }

    @Nested
    inner class UpdateProgress {
        @Test
        fun `calls replayUpdateBookProgress with the payload's fields including actionAt and returns SYNCED`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookReadId = 100,
                editionId = 200,
                progressPages = 50,
                progressSeconds = 3600,
                startedAt = "2026-01-01",
                finishedAt = null,
                actionAt = "2026-05-04T12:34:56Z",
            )

            coJustRun {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = 100,
                    editionId = 200,
                    progressPages = 50,
                    progressSeconds = 3600,
                    startedAt = "2026-01-01",
                    finishedAt = null,
                    actionAt = "2026-05-04T12:34:56Z",
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
                    actionAt = "2026-05-04T12:34:56Z",
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

        @Test
        fun `marks exactly the payload's history row as synced`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                progressHistoryId = 9L,
            )
            stubReplayProgress()

            // ----- Act -----
            replay(payload = payload)

            // ----- Assert -----
            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.markSynced(localId = 9L)
            }

            coVerify(exactly = 0) {
                progressHistoryLocalDataSource.delete(localId = any())
            }
        }

        @Test
        fun `touches no history row when the payload has no progressHistoryId`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                progressHistoryId = null,
            )
            stubReplayProgress()

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            verify { progressHistoryLocalDataSource wasNot Called }
        }

        @Test
        fun `touches no history row when the payload has no progressHistoryId and the replay throws`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                progressHistoryId = null,
            )
            stubReplayProgressThrowing(error = RuntimeException("rejected"))

            // ----- Act -----
            runCatching { replay(payload = payload) }

            // ----- Assert -----
            verify { progressHistoryLocalDataSource wasNot Called }
        }

        @Test
        fun `deletes the history row and rethrows when the replay fails with a non-retryable exception`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                progressHistoryId = 9L,
            )
            val rejection = RuntimeException("rejected")
            stubReplayProgressThrowing(error = rejection)

            // ----- Act & Assert -----
            val thrown = shouldThrow<RuntimeException> { replay(payload = payload) }

            thrown shouldBe rejection

            // ----- Assert -----
            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.delete(localId = 9L)
            }

            coVerify(exactly = 0) {
                progressHistoryLocalDataSource.markSynced(localId = any())
            }
        }

        @Test
        fun `keeps the history row pending and rethrows when the replay fails with a retryable exception`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                progressHistoryId = 9L,
            )
            val transient = ServerUnavailableException(message = "down")
            stubReplayProgressThrowing(error = transient)

            // ----- Act & Assert -----
            val thrown = shouldThrow<ServerUnavailableException> { replay(payload = payload) }

            thrown shouldBe transient

            // ----- Assert -----
            coVerify(exactly = 0) {
                progressHistoryLocalDataSource.markSynced(localId = any())
            }

            coVerify(exactly = 0) {
                progressHistoryLocalDataSource.delete(localId = any())
            }
        }
    }

    @Nested
    inner class MarkAsRead {
        @Test
        fun `calls replayMarkBookAsRead with the payload edition id and the local date of enqueuedAt and returns SYNCED`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.MARK_AS_READ,
                bookId = 300,
                editionId = 321,
                enqueuedAt = "2026-05-04T12:34:56Z",
                actionAt = "2026-05-04T12:34:56Z",
            )

            coJustRun {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = 300,
                    editionId = 321,
                    userDate = localDateOf(actionAt = "2026-05-04T12:34:56Z"),
                    actionAt = "2026-05-04T12:34:56Z",
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = 300,
                    editionId = 321,
                    userDate = localDateOf(actionAt = "2026-05-04T12:34:56Z"),
                    actionAt = "2026-05-04T12:34:56Z",
                )
            }
        }

        @Test
        fun `does not touch the progress history`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(kind = PendingUserBookWriteKind.MARK_AS_READ)

            coJustRun {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }

            // ----- Act -----
            replay(payload = payload)

            // ----- Assert -----
            verify { progressHistoryLocalDataSource wasNot Called }
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
        fun `passes hasSpoilers and the local date of enqueuedAt for a valid review body`() = runTest {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"))
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_REVIEW,
                userBookId = 40,
                reviewSlateJson = review.toJson(),
                reviewHasSpoilers = true,
                enqueuedAt = "2026-05-30T12:15:30Z",
            )

            coJustRun {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = 40,
                    review = review,
                    hasSpoilers = true,
                    reviewedAt = "2026-05-31",
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
                    reviewedAt = "2026-05-31",
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

    @Nested
    inner class SetStatus {
        @Test
        fun `calls replayUpdateUserBookStatus with the stored status code and the enqueue date and returns SYNCED`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.SET_STATUS,
                userBookId = 40,
                statusCode = 3,
                enqueuedAt = "2026-05-04T12:34:56Z",
            )

            coJustRun {
                booksRemoteDataSource.replayUpdateUserBookStatus(
                    userBookId = 40,
                    statusCode = 3,
                    userDate = localDateOf(actionAt = "2026-05-04T12:34:56Z"),
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateUserBookStatus(
                    userBookId = 40,
                    statusCode = 3,
                    userDate = localDateOf(actionAt = "2026-05-04T12:34:56Z"),
                )
            }
        }

        @Test
        fun `returns DISCARDED and calls nothing when statusCode is null`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.SET_STATUS,
                statusCode = null,
            )

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.DISCARDED
            verify { booksRemoteDataSource wasNot Called }
        }

        @Test
        fun `propagates a remote failure so the drainer can retry`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.SET_STATUS,
                statusCode = 2,
            )

            val transient = ServerUnavailableException(message = "down")

            coEvery {
                booksRemoteDataSource.replayUpdateUserBookStatus(
                    userBookId = any(),
                    statusCode = any(),
                    userDate = any(),
                )
            } throws transient

            // ----- Act -----
            val thrown = shouldThrow<ServerUnavailableException> { replay(payload = payload) }

            // ----- Assert -----
            thrown shouldBe transient
        }
    }
}
