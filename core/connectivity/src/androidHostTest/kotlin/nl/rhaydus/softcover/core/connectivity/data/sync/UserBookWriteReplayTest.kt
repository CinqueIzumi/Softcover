package nl.rhaydus.softcover.core.connectivity.data.sync

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import nl.rhaydus.offlinesync.ReplayOutcome
import nl.rhaydus.softcover.core.book.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.core.book.data.datasource.ProgressHistoryLocalDataSource
import nl.rhaydus.softcover.core.book.domain.model.ServerUserBookRead
import nl.rhaydus.softcover.core.book.domain.model.ServerUserBookState
import nl.rhaydus.softcover.core.book.domain.model.UnfinishedRead
import nl.rhaydus.softcover.core.database.mapper.toJson
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteLock
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteQueue
import nl.rhaydus.softcover.core.domain.exception.ServerUnavailableException
import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.domain.util.localDateOf
import java.util.TimeZone
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserBookWriteReplayTest {
    private lateinit var booksRemoteDataSource: BooksRemoteDataSource
    private lateinit var progressHistoryLocalDataSource: ProgressHistoryLocalDataSource
    private lateinit var writeLock: UserBookWriteLock
    private lateinit var queue: UserBookWriteQueue
    private lateinit var replay: UserBookWriteReplay
    private lateinit var originalZone: TimeZone

    @BeforeEach
    fun setUp() {
        originalZone = TimeZone.getDefault()
        booksRemoteDataSource = mockk()
        progressHistoryLocalDataSource = mockk(relaxed = true)
        writeLock = UserBookWriteLock()
        queue = mockk()
        coEvery {
            queue.isCurrent(write = any())
        } returns true
        coEvery {
            booksRemoteDataSource.fetchUserBookState(bookId = any())
        } returns null
        replay = UserBookWriteReplay(
            booksRemoteDataSource = booksRemoteDataSource,
            progressHistoryLocalDataSource = progressHistoryLocalDataSource,
            writeLock = writeLock,
            queue = queue,
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

    private fun stubServerState(
        statusCode: Int? = null,
        rating: Double? = null,
        reviewDocument: ReviewDocument? = null,
        reviewHasSpoilers: Boolean = false,
        reads: List<ServerUserBookRead> = emptyList(),
    ) {
        val state = ServerUserBookState(
            statusCode = statusCode,
            rating = rating,
            reviewDocument = reviewDocument,
            reviewHasSpoilers = reviewHasSpoilers,
            reads = reads,
        )

        coEvery {
            booksRemoteDataSource.fetchUserBookState(bookId = any())
        } returns state
    }

    private fun serverRead(
        id: Int = 100,
        progressPages: Int? = null,
        progressSeconds: Int? = null,
        finishedAt: String? = null,
    ) = ServerUserBookRead(
        id = id,
        progressPages = progressPages,
        progressSeconds = progressSeconds,
        finishedAt = finishedAt,
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
        fun `deletes the history row and rethrows when the server rejects the progress with an UnexpectedApiException`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                progressHistoryId = 9L,
            )
            val rejection = UnexpectedApiException(message = "Progress update rejected: nope")
            stubReplayProgressThrowing(error = rejection)

            // ----- Act & Assert -----
            val thrown = shouldThrow<UnexpectedApiException> { replay(payload = payload) }

            thrown shouldBe rejection

            // ----- Assert -----
            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.delete(localId = 9L)
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
    inner class StaleRow {
        @Test
        fun `returns DISCARDED without calling the remote or touching history when the row is no longer current`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                progressHistoryId = 9L,
            )

            coEvery {
                queue.isCurrent(write = payload)
            } returns false

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.DISCARDED
            verify { booksRemoteDataSource wasNot Called }
            verify { progressHistoryLocalDataSource wasNot Called }
        }
    }

    @Nested
    inner class WriteLocking {
        @Test
        fun `waits for a holder of the same book's lock before calling the remote`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                bookId = 20,
            )
            val release = CompletableDeferred<Unit>()
            val holderEntered = CompletableDeferred<Unit>()

            stubReplayProgress()

            launch {
                writeLock.withLock(bookId = 20) {
                    holderEntered.complete(Unit)
                    release.await()
                }
            }

            holderEntered.await()

            // ----- Act -----
            val replayed = launch { replay(payload = payload) }

            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 0) {
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

            release.complete(Unit)
            replayed.join()

            coVerify(exactly = 1) {
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
    }

    @Nested
    inner class PlaceholderRead {
        @Test
        fun `replays against the resolved read id and start date with no finish date and returns SYNCED`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookId = 10,
                userBookReadId = -7,
                editionId = 200,
                progressPages = 50,
                progressSeconds = 3600,
                startedAt = "2026-01-01",
                finishedAt = "2026-02-02",
                actionAt = "2026-05-04T12:34:56Z",
                progressHistoryId = 9L,
            )

            coEvery {
                booksRemoteDataSource.fetchLatestUnfinishedRead(userBookId = 10)
            } returns UnfinishedRead(
                id = 555,
                startedAt = "2026-03-03",
            )
            stubReplayProgress()

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = 555,
                    editionId = 200,
                    progressPages = 50,
                    progressSeconds = 3600,
                    startedAt = "2026-03-03",
                    finishedAt = null,
                    actionAt = "2026-05-04T12:34:56Z",
                )
            }

            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.markSynced(localId = 9L)
            }
        }

        @Test
        fun `replays with a null start date when the resolved read has none`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookReadId = -1,
                startedAt = "2026-01-01",
            )

            coEvery {
                booksRemoteDataSource.fetchLatestUnfinishedRead(userBookId = any())
            } returns UnfinishedRead(
                id = 555,
                startedAt = null,
            )
            stubReplayProgress()

            // ----- Act -----
            replay(payload = payload)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = 555,
                    editionId = any(),
                    progressPages = any(),
                    progressSeconds = any(),
                    startedAt = null,
                    finishedAt = null,
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `returns DISCARDED, replays nothing and deletes the history row when no unfinished read exists`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookId = 10,
                userBookReadId = -7,
                progressHistoryId = 9L,
            )

            coEvery {
                booksRemoteDataSource.fetchLatestUnfinishedRead(userBookId = 10)
            } returns null

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.DISCARDED
            coVerify(exactly = 0) {
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

            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.delete(localId = 9L)
            }

            coVerify(exactly = 0) {
                progressHistoryLocalDataSource.markSynced(localId = any())
            }
        }

        @Test
        fun `returns DISCARDED without touching history when no unfinished read exists and the payload has no history row`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookReadId = -7,
                progressHistoryId = null,
            )

            coEvery {
                booksRemoteDataSource.fetchLatestUnfinishedRead(userBookId = any())
            } returns null

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.DISCARDED
            verify { progressHistoryLocalDataSource wasNot Called }
        }

        @Test
        fun `keeps the history row pending and rethrows when resolving the read fails with a retryable exception`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookReadId = -7,
                progressHistoryId = 9L,
            )
            val transient = ServerUnavailableException(message = "down")

            coEvery {
                booksRemoteDataSource.fetchLatestUnfinishedRead(userBookId = any())
            } throws transient

            // ----- Act & Assert -----
            val thrown = shouldThrow<ServerUnavailableException> { replay(payload = payload) }

            thrown shouldBe transient

            // ----- Assert -----
            verify { progressHistoryLocalDataSource wasNot Called }
        }

        @Test
        fun `never resolves a read for a positive read id`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookReadId = 100,
            )
            stubReplayProgress()

            // ----- Act -----
            replay(payload = payload)

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchLatestUnfinishedRead(userBookId = any())
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

    @Nested
    inner class AlreadyAppliedOnServer {
        @Test
        fun `progress returns SYNCED without sending and marks the history row synced when the read already has the pages`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookReadId = 100,
                progressPages = 50,
                progressSeconds = null,
                progressHistoryId = 9L,
            )
            val read = serverRead(
                id = 100,
                progressPages = 50,
            )
            stubServerState(reads = listOf(read))

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 0) {
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

            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.markSynced(localId = 9L)
            }
        }

        @Test
        fun `progress sends when the read has a different page count`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookReadId = 100,
                progressPages = 50,
                progressSeconds = null,
            )
            val read = serverRead(
                id = 100,
                progressPages = 40,
            )
            stubServerState(reads = listOf(read))
            stubReplayProgress()

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = 100,
                    editionId = any(),
                    progressPages = 50,
                    progressSeconds = any(),
                    startedAt = any(),
                    finishedAt = any(),
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `progress compares seconds for an audio payload even when the pages differ`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookReadId = 100,
                progressPages = 50,
                progressSeconds = 3600,
            )
            val read = serverRead(
                id = 100,
                progressPages = 1,
                progressSeconds = 3600,
            )
            stubServerState(reads = listOf(read))

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 0) {
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

        @Test
        fun `progress sends for an audio payload when the seconds differ though the pages match`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookReadId = 100,
                progressPages = 50,
                progressSeconds = 3600,
            )
            val read = serverRead(
                id = 100,
                progressPages = 50,
                progressSeconds = 60,
            )
            stubServerState(reads = listOf(read))
            stubReplayProgress()

            // ----- Act -----
            replay(payload = payload)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = 100,
                    editionId = any(),
                    progressPages = any(),
                    progressSeconds = 3600,
                    startedAt = any(),
                    finishedAt = any(),
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `progress sends when the matching page sits on a different read id`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookReadId = 100,
                progressPages = 50,
                progressSeconds = null,
            )
            val read = serverRead(
                id = 101,
                progressPages = 50,
            )
            stubServerState(reads = listOf(read))
            stubReplayProgress()

            // ----- Act -----
            replay(payload = payload)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = 100,
                    editionId = any(),
                    progressPages = 50,
                    progressSeconds = any(),
                    startedAt = any(),
                    finishedAt = any(),
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `progress on a placeholder read checks the resolved read id for the applied pages`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookId = 10,
                userBookReadId = -7,
                progressPages = 50,
                progressSeconds = null,
                progressHistoryId = 9L,
            )

            coEvery {
                booksRemoteDataSource.fetchLatestUnfinishedRead(userBookId = 10)
            } returns UnfinishedRead(
                id = 555,
                startedAt = "2026-03-03",
            )
            val read = serverRead(
                id = 555,
                progressPages = 50,
            )
            stubServerState(reads = listOf(read))

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 0) {
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

            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.markSynced(localId = 9L)
            }
        }

        @Test
        fun `mark as read returns SYNCED without sending when the book is Read with a read finished on the action date`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.MARK_AS_READ,
                enqueuedAt = "2026-05-04T12:34:56Z",
                actionAt = "2026-05-04T12:34:56Z",
            )
            val finishedOn = localDateOf(actionAt = "2026-05-04T12:34:56Z")
            stubServerState(
                statusCode = UserBookStatus.READ.code,
                reads = listOf(serverRead(finishedAt = finishedOn)),
            )

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 0) {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `mark as read sends when the only finished read is on another date`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.MARK_AS_READ,
                enqueuedAt = "2026-05-04T12:34:56Z",
                actionAt = "2026-05-04T12:34:56Z",
            )
            stubServerState(
                statusCode = UserBookStatus.READ.code,
                reads = listOf(serverRead(finishedAt = "2020-01-01")),
            )
            coJustRun {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 1) {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `mark as read sends when a read finished on the date but the status is not Read`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.MARK_AS_READ,
                enqueuedAt = "2026-05-04T12:34:56Z",
                actionAt = "2026-05-04T12:34:56Z",
            )
            stubServerState(
                statusCode = UserBookStatus.CURRENTLY_READING.code,
                reads = listOf(serverRead(finishedAt = localDateOf(actionAt = "2026-05-04T12:34:56Z"))),
            )
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
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `status returns SYNCED without sending when the server already holds the status`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.SET_STATUS,
                statusCode = 3,
            )
            stubServerState(statusCode = 3)

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 0) {
                booksRemoteDataSource.replayUpdateUserBookStatus(
                    userBookId = any(),
                    statusCode = any(),
                    userDate = any(),
                )
            }
        }

        @Test
        fun `status sends when the server holds a different status`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.SET_STATUS,
                statusCode = 3,
            )
            stubServerState(statusCode = 2)
            coJustRun {
                booksRemoteDataSource.replayUpdateUserBookStatus(
                    userBookId = any(),
                    statusCode = any(),
                    userDate = any(),
                )
            }

            // ----- Act -----
            replay(payload = payload)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateUserBookStatus(
                    userBookId = any(),
                    statusCode = 3,
                    userDate = any(),
                )
            }
        }

        @Test
        fun `rating returns SYNCED without sending when the server already holds the rating`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_RATING,
                rating = 4.5,
            )
            stubServerState(rating = 4.5)

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 0) {
                booksRemoteDataSource.replayUpdateBookRating(
                    userBookId = any(),
                    rating = any(),
                )
            }
        }

        @Test
        fun `rating sends when the server holds a different rating`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_RATING,
                rating = 4.5,
            )
            stubServerState(rating = 3.0)
            coJustRun {
                booksRemoteDataSource.replayUpdateBookRating(
                    userBookId = any(),
                    rating = any(),
                )
            }

            // ----- Act -----
            replay(payload = payload)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookRating(
                    userBookId = any(),
                    rating = 4.5,
                )
            }
        }

        private fun reviewOf(vararg texts: String): ReviewDocument {
            val runs = texts.map { ReviewRun(text = it) }

            return ReviewDocument(paragraphs = listOf(ReviewParagraph(runs = runs)))
        }

        @Test
        fun `review returns SYNCED without sending when the server already holds the same body and spoiler flag`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_REVIEW,
                reviewSlateJson = reviewOf("Great book!").toJson(),
                reviewHasSpoilers = true,
            )
            stubServerState(
                reviewDocument = reviewOf("Great book!"),
                reviewHasSpoilers = true,
            )
            coJustRun {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 0) {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }
        }

        @Test
        fun `review returns SYNCED without sending when the server body differs only by how its runs are split`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_REVIEW,
                reviewSlateJson = reviewOf("Great book!").toJson(),
                reviewHasSpoilers = false,
            )
            stubServerState(
                reviewDocument = reviewOf(
                    "Great ",
                    "",
                    "book!",
                ),
                reviewHasSpoilers = false,
            )
            coJustRun {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 0) {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }
        }

        @Test
        fun `review sends when the server body differs`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_REVIEW,
                reviewSlateJson = reviewOf("Great book!").toJson(),
                reviewHasSpoilers = false,
            )
            stubServerState(
                reviewDocument = reviewOf("Okay book."),
                reviewHasSpoilers = false,
            )
            coJustRun {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }
        }

        @Test
        fun `review sends when the body matches but the spoiler flag differs`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_REVIEW,
                reviewSlateJson = reviewOf("Great book!").toJson(),
                reviewHasSpoilers = true,
            )
            stubServerState(
                reviewDocument = reviewOf("Great book!"),
                reviewHasSpoilers = false,
            )
            coJustRun {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }
        }

        @Test
        fun `review sends when the server holds no review`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_REVIEW,
                reviewSlateJson = reviewOf("Great book!").toJson(),
                reviewHasSpoilers = false,
            )
            stubServerState(
                reviewDocument = null,
                reviewHasSpoilers = false,
            )
            coJustRun {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }
        }

        @Test
        fun `mark as read returns SYNCED without sending when a read finished one day before the action date`() = runTest {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.MARK_AS_READ,
                enqueuedAt = "2026-05-04T12:34:56Z",
                actionAt = "2026-05-04T12:34:56Z",
            )
            stubServerState(
                statusCode = UserBookStatus.READ.code,
                reads = listOf(serverRead(finishedAt = "2026-05-03")),
            )
            coJustRun {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 0) {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `mark as read returns SYNCED without sending when a read finished one day after the action date`() = runTest {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.MARK_AS_READ,
                enqueuedAt = "2026-05-04T12:34:56Z",
                actionAt = "2026-05-04T12:34:56Z",
            )
            stubServerState(
                statusCode = UserBookStatus.READ.code,
                reads = listOf(serverRead(finishedAt = "2026-05-05")),
            )
            coJustRun {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 0) {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `mark as read sends when the finished read is two days before the action date`() = runTest {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.MARK_AS_READ,
                enqueuedAt = "2026-05-04T12:34:56Z",
                actionAt = "2026-05-04T12:34:56Z",
            )
            stubServerState(
                statusCode = UserBookStatus.READ.code,
                reads = listOf(serverRead(finishedAt = "2026-05-02")),
            )
            coJustRun {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 1) {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `mark as read sends when the finished read is two days after the action date`() = runTest {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.MARK_AS_READ,
                enqueuedAt = "2026-05-04T12:34:56Z",
                actionAt = "2026-05-04T12:34:56Z",
            )
            stubServerState(
                statusCode = UserBookStatus.READ.code,
                reads = listOf(serverRead(finishedAt = "2026-05-06")),
            )
            coJustRun {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 1) {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `mark as read sends when the finished date does not parse`() = runTest {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.MARK_AS_READ,
                enqueuedAt = "2026-05-04T12:34:56Z",
                actionAt = "2026-05-04T12:34:56Z",
            )
            stubServerState(
                statusCode = UserBookStatus.READ.code,
                reads = listOf(serverRead(finishedAt = "not-a-date")),
            )
            coJustRun {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }

            // ----- Act -----
            val result = replay(payload = payload)

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 1) {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    editionId = any(),
                    userDate = any(),
                    actionAt = any(),
                )
            }
        }
    }

    @Nested
    inner class FailingStateFetch {
        @Test
        fun `progress propagates a retryable state fetch failure and leaves the history row pending`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                progressHistoryId = 9L,
            )
            val transient = ServerUnavailableException(message = "down")
            coEvery {
                booksRemoteDataSource.fetchUserBookState(bookId = any())
            } throws transient

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

        @Test
        fun `status propagates a state fetch failure without sending`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.SET_STATUS,
                statusCode = 3,
            )
            val transient = ServerUnavailableException(message = "down")
            coEvery {
                booksRemoteDataSource.fetchUserBookState(bookId = any())
            } throws transient

            // ----- Act & Assert -----
            val thrown = shouldThrow<ServerUnavailableException> { replay(payload = payload) }

            thrown shouldBe transient
        }
    }
}
