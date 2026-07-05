package nl.rhaydus.softcover.core.connectivity.data.sync

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.core.database.dao.PendingUserBookWriteDao
import nl.rhaydus.softcover.core.database.mapper.toJson
import nl.rhaydus.softcover.core.database.model.PendingUserBookWriteEntity
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.exception.ServerUnavailableException
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PendingUserBookWriteSyncerTest {
    private lateinit var networkAvailability: NetworkAvailabilityProvider
    private lateinit var dao: PendingUserBookWriteDao
    private lateinit var booksRemoteDataSource: BooksRemoteDataSource
    private lateinit var syncer: PendingUserBookWriteSyncer
    private lateinit var appDispatchers: AppDispatchers

    @BeforeEach
    fun setUp() {
        networkAvailability = mockk()
        dao = mockk()
        booksRemoteDataSource = mockk()

        val dispatcher = UnconfinedTestDispatcher()
        appDispatchers = AppDispatchers(
            main = dispatcher,
            io = dispatcher,
            default = dispatcher,
        )

        syncer = PendingUserBookWriteSyncer(
            networkAvailability = networkAvailability,
            dao = dao,
            booksRemoteDataSource = booksRemoteDataSource,
            appDispatchers = appDispatchers,
        )
    }

    private fun updateProgressEntity(
        localId: Long = 1L,
        userBookId: Int = 10,
        userBookReadId: Int = 100,
        editionId: Int? = 200,
        progressPages: Int? = 50,
        progressSeconds: Int? = 3600,
        startedAt: String? = "2026-01-01",
        finishedAt: String? = null,
        enqueuedAt: String = "2026-05-04T12:00:00Z",
    ) = PendingUserBookWriteEntity(
        localId = localId,
        kind = PendingUserBookWriteKind.UPDATE_PROGRESS.name,
        userBookId = userBookId,
        userBookReadId = userBookReadId,
        bookId = 0,
        editionId = editionId,
        progressPages = progressPages,
        progressSeconds = progressSeconds,
        startedAt = startedAt,
        finishedAt = finishedAt,
        enqueuedAt = enqueuedAt,
    )

    private fun updateRatingEntity(
        localId: Long = 3L,
        userBookId: Int = 30,
        rating: Double? = 4.5,
        enqueuedAt: String = "2026-05-04T14:00:00Z",
    ) = PendingUserBookWriteEntity(
        localId = localId,
        kind = PendingUserBookWriteKind.UPDATE_RATING.name,
        userBookId = userBookId,
        userBookReadId = 0,
        bookId = 0,
        editionId = null,
        progressPages = null,
        progressSeconds = null,
        startedAt = null,
        finishedAt = null,
        rating = rating,
        enqueuedAt = enqueuedAt,
    )

    private fun updateReviewEntity(
        localId: Long = 4L,
        userBookId: Int = 40,
        reviewSlateJson: String? = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Great book!"))))).toJson(),
        reviewHasSpoilers: Boolean? = false,
        enqueuedAt: String = "2026-05-30T10:15:30Z",
    ) = PendingUserBookWriteEntity(
        localId = localId,
        kind = PendingUserBookWriteKind.UPDATE_REVIEW.name,
        userBookId = userBookId,
        userBookReadId = 0,
        bookId = 0,
        editionId = null,
        progressPages = null,
        progressSeconds = null,
        startedAt = null,
        finishedAt = null,
        reviewSlateJson = reviewSlateJson,
        reviewHasSpoilers = reviewHasSpoilers,
        enqueuedAt = enqueuedAt,
    )

    private fun markAsReadEntity(
        localId: Long = 2L,
        userBookId: Int = 20,
        bookId: Int = 300,
        enqueuedAt: String = "2026-05-04T12:34:56Z",
    ) = PendingUserBookWriteEntity(
        localId = localId,
        kind = PendingUserBookWriteKind.MARK_AS_READ.name,
        userBookId = userBookId,
        userBookReadId = 0,
        bookId = bookId,
        editionId = null,
        progressPages = null,
        progressSeconds = null,
        startedAt = null,
        finishedAt = null,
        enqueuedAt = enqueuedAt,
    )

    @Nested
    inner class DrainPendingUpdates {
        @Test
        fun `drains pending entries and returns their userBookIds`() = runTest {
            // ----- Arrange -----
            val entityA = updateProgressEntity(
                localId = 1L,
                userBookId = 10,
            )
            val entityB = markAsReadEntity(
                localId = 2L,
                userBookId = 20,
            )

            coEvery {
                dao.getPending()
            } returns listOf(entityA, entityB)

            coJustRun {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = any(),
                    editionId = any(),
                    progressPages = any(),
                    progressSeconds = any(),
                    startedAt = any(),
                    finishedAt = any(),
                )
            }

            coJustRun {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    userDate = any(),
                )
            }

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            val result = syncer.drainPendingUpdates()

            // ----- Assert -----
            result shouldBe mapOf(
                10 to setOf(PendingUserBookWriteKind.UPDATE_PROGRESS),
                20 to setOf(PendingUserBookWriteKind.MARK_AS_READ),
            )
            result.contains(10) shouldBe true
            result.contains(20) shouldBe true
            coVerify(exactly = 1) { dao.delete(1L) }
            coVerify(exactly = 1) { dao.delete(2L) }
        }

        @Test
        fun `consumes the recently-synced buffer so a second call returns empty`() = runTest {
            // ----- Arrange -----
            val entityA = updateProgressEntity(
                localId = 1L,
                userBookId = 10,
            )
            val entityB = markAsReadEntity(
                localId = 2L,
                userBookId = 20,
            )

            coEvery {
                dao.getPending()
            } returnsMany listOf(
                listOf(entityA, entityB),
                emptyList(),
            )

            coJustRun {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = any(),
                    editionId = any(),
                    progressPages = any(),
                    progressSeconds = any(),
                    startedAt = any(),
                    finishedAt = any(),
                )
            }

            coJustRun {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    userDate = any(),
                )
            }

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            val firstResult = syncer.drainPendingUpdates()
            val secondResult = syncer.drainPendingUpdates()

            // ----- Assert -----
            firstResult shouldBe mapOf(
                10 to setOf(PendingUserBookWriteKind.UPDATE_PROGRESS),
                20 to setOf(PendingUserBookWriteKind.MARK_AS_READ),
            )
            secondResult shouldBe emptyMap()
        }

        @Test
        fun `surfaces ids successfully synced by the flow-triggered drain when the queue is later empty`() = runTest {
            // ----- Arrange -----
            val isOnlineFlow = MutableStateFlow(false)
            val entity = markAsReadEntity(
                localId = 5L,
                userBookId = 42,
            )

            coEvery {
                networkAvailability.isOnline
            } returns isOnlineFlow

            coEvery {
                dao.getPending()
            } returnsMany listOf(
                listOf(entity),
                emptyList(),
            )

            coJustRun {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    userDate = any(),
                )
            }

            coJustRun {
                dao.delete(any())
            }

            syncer.start(backgroundScope)
            isOnlineFlow.value = true
            advanceUntilIdle()

            // ----- Act -----
            val result = syncer.drainPendingUpdates()

            // ----- Assert -----
            result shouldBe mapOf(42 to setOf(PendingUserBookWriteKind.MARK_AS_READ))
        }

        @Test
        fun `transient retryable failure increments attempts and halts further drains`() = runTest {
            // ----- Arrange -----
            val entityA = updateProgressEntity(
                localId = 1L,
                userBookId = 10,
                userBookReadId = 101,
                editionId = 201,
            )
            val entityB = updateProgressEntity(
                localId = 2L,
                userBookId = 20,
                userBookReadId = 102,
                editionId = 202,
            )
            val entityC = updateProgressEntity(
                localId = 3L,
                userBookId = 30,
                userBookReadId = 103,
                editionId = 203,
            )

            coEvery {
                dao.getPending()
            } returns listOf(entityA, entityB, entityC)

            coEvery {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = 101,
                    editionId = 201,
                    progressPages = any(),
                    progressSeconds = any(),
                    startedAt = any(),
                    finishedAt = any(),
                )
            } throws ServerUnavailableException("server unreachable")

            coJustRun {
                dao.incrementAttempts(any())
            }

            // ----- Act -----
            val result = syncer.drainPendingUpdates()

            // ----- Assert -----
            result shouldBe emptyMap()
            coVerify(exactly = 1) { dao.incrementAttempts(1L) }
            coVerify(exactly = 0) { dao.delete(any()) }
            coVerify(exactly = 0) {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = 102,
                    editionId = any(),
                    progressPages = any(),
                    progressSeconds = any(),
                    startedAt = any(),
                    finishedAt = any(),
                )
            }
            coVerify(exactly = 0) {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = 103,
                    editionId = any(),
                    progressPages = any(),
                    progressSeconds = any(),
                    startedAt = any(),
                    finishedAt = any(),
                )
            }
        }

        @Test
        fun `non-retryable failure deletes the row and continues draining remaining entities`() = runTest {
            // ----- Arrange -----
            val entityA = updateProgressEntity(
                localId = 1L,
                userBookId = 10,
                userBookReadId = 101,
                editionId = 201,
            )
            val entityB = updateProgressEntity(
                localId = 2L,
                userBookId = 20,
                userBookReadId = 102,
                editionId = 202,
            )

            coEvery {
                dao.getPending()
            } returns listOf(entityA, entityB)

            coEvery {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = 101,
                    editionId = 201,
                    progressPages = any(),
                    progressSeconds = any(),
                    startedAt = any(),
                    finishedAt = any(),
                )
            } throws RuntimeException("rejected by server")

            coJustRun {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = 102,
                    editionId = 202,
                    progressPages = any(),
                    progressSeconds = any(),
                    startedAt = any(),
                    finishedAt = any(),
                )
            }

            coJustRun { dao.delete(any()) }

            // ----- Act -----
            val result = syncer.drainPendingUpdates()

            // ----- Assert -----
            coVerify(exactly = 1) { dao.delete(1L) }
            coVerify(exactly = 1) { dao.delete(2L) }
            coVerify(exactly = 0) { dao.incrementAttempts(any()) }
            result shouldBe mapOf(entityB.userBookId to setOf(PendingUserBookWriteKind.UPDATE_PROGRESS))
            result.contains(entityA.userBookId) shouldBe false
        }

        @Test
        fun `delegates UPDATE_PROGRESS to replayUpdateBookProgress with all entity fields`() = runTest {
            // ----- Arrange -----
            val entity = updateProgressEntity(
                localId = 7L,
                userBookId = 99,
                userBookReadId = 555,
                editionId = 888,
                progressPages = 120,
                progressSeconds = 7200,
                startedAt = "2026-03-01",
                finishedAt = "2026-04-15",
            )

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coJustRun {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = any(),
                    editionId = any(),
                    progressPages = any(),
                    progressSeconds = any(),
                    startedAt = any(),
                    finishedAt = any(),
                )
            }

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            val result = syncer.drainPendingUpdates()

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = 555,
                    editionId = 888,
                    progressPages = 120,
                    progressSeconds = 7200,
                    startedAt = "2026-03-01",
                    finishedAt = "2026-04-15",
                )
            }

            result shouldBe mapOf(99 to setOf(PendingUserBookWriteKind.UPDATE_PROGRESS))
        }

        @Test
        fun `delegates MARK_AS_READ to replayMarkBookAsRead with bookId and date prefix of enqueuedAt`() = runTest {
            // ----- Arrange -----
            val entity = markAsReadEntity(
                localId = 3L,
                userBookId = 77,
                bookId = 404,
                enqueuedAt = "2026-05-04T12:34:56Z",
            )

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coJustRun {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    userDate = any(),
                )
            }

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            val result = syncer.drainPendingUpdates()

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = 404,
                    userDate = "2026-05-04",
                )
            }

            result shouldBe mapOf(77 to setOf(PendingUserBookWriteKind.MARK_AS_READ))
        }

        @Test
        fun `delegates UPDATE_RATING with non-null rating to replayUpdateBookRating and deletes entity`() = runTest {
            // ----- Arrange -----
            val entity = updateRatingEntity(
                localId = 8L,
                userBookId = 55,
                rating = 3.5,
            )

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coJustRun {
                booksRemoteDataSource.replayUpdateBookRating(
                    userBookId = any(),
                    rating = any(),
                )
            }

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            val result = syncer.drainPendingUpdates()

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookRating(
                    userBookId = 55,
                    rating = 3.5,
                )
            }

            coVerify(exactly = 1) { dao.delete(8L) }

            result shouldBe mapOf(55 to setOf(PendingUserBookWriteKind.UPDATE_RATING))
        }

        @Test
        fun `drops UPDATE_RATING entity with null rating without calling replayUpdateBookRating`() = runTest {
            // ----- Arrange -----
            val entity = updateRatingEntity(
                localId = 9L,
                userBookId = 66,
                rating = null,
            )

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            val result = syncer.drainPendingUpdates()

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.replayUpdateBookRating(
                    userBookId = any(),
                    rating = any(),
                )
            }

            coVerify(exactly = 1) { dao.delete(9L) }

            result.contains(66) shouldBe false
        }

        @Test
        fun `delegates UPDATE_REVIEW with non-null reviewSlateJson to replayUpdateBookReview and deletes entity`() = runTest {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Loved it")))))
            val entity = updateReviewEntity(
                localId = 12L,
                userBookId = 80,
                reviewSlateJson = doc.toJson(),
                reviewHasSpoilers = true,
                enqueuedAt = "2026-05-30T10:15:30Z",
            )

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coJustRun {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            val result = syncer.drainPendingUpdates()

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = 80,
                    review = doc,
                    hasSpoilers = true,
                    reviewedAt = "2026-05-30",
                )
            }

            coVerify(exactly = 1) { dao.delete(12L) }

            result shouldBe mapOf(80 to setOf(PendingUserBookWriteKind.UPDATE_REVIEW))
        }

        @Test
        fun `drops UPDATE_REVIEW entity with null reviewSlateJson without calling replayUpdateBookReview`() = runTest {
            // ----- Arrange -----
            val entity = updateReviewEntity(
                localId = 13L,
                userBookId = 90,
                reviewSlateJson = null,
            )

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            val result = syncer.drainPendingUpdates()

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }

            coVerify(exactly = 1) { dao.delete(13L) }

            result.contains(90) shouldBe false
        }

        @Test
        fun `passes the date part before T as reviewedAt for UPDATE_REVIEW`() = runTest {
            // ----- Arrange -----
            val doc = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("A book")))))
            val entity = updateReviewEntity(
                localId = 14L,
                userBookId = 95,
                reviewSlateJson = doc.toJson(),
                reviewHasSpoilers = false,
                enqueuedAt = "2026-05-30T10:15:30Z",
            )

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coJustRun {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            syncer.drainPendingUpdates()

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.replayUpdateBookReview(
                    userBookId = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = "2026-05-30",
                )
            }
        }

        @Test
        fun `discards entity with unknown kind — deletes row but excludes userBookId from synced set`() = runTest {
            // ----- Arrange -----
            val entity = PendingUserBookWriteEntity(
                localId = 11L,
                kind = "SOME_UNKNOWN_KIND",
                userBookId = 77,
                userBookReadId = 0,
                bookId = 0,
                editionId = null,
                progressPages = null,
                progressSeconds = null,
                startedAt = null,
                finishedAt = null,
                enqueuedAt = "2026-05-04T12:00:00Z",
            )

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            val result = syncer.drainPendingUpdates()

            // ----- Assert -----
            coVerify(exactly = 1) { dao.delete(11L) }

            coVerify(exactly = 0) {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = any(),
                    editionId = any(),
                    progressPages = any(),
                    progressSeconds = any(),
                    startedAt = any(),
                    finishedAt = any(),
                )
            }

            coVerify(exactly = 0) {
                booksRemoteDataSource.replayMarkBookAsRead(
                    bookId = any(),
                    userDate = any(),
                )
            }

            coVerify(exactly = 0) {
                booksRemoteDataSource.replayUpdateBookRating(
                    userBookId = any(),
                    rating = any(),
                )
            }

            result.contains(77) shouldBe false
        }

        @Test
        fun `two kinds for the same userBookId are both recorded in the returned map`() = runTest {
            // ----- Arrange -----
            val userBookId = 50
            val entityProgress = updateProgressEntity(
                localId = 20L,
                userBookId = userBookId,
            )
            val entityRating = updateRatingEntity(
                localId = 21L,
                userBookId = userBookId,
                rating = 4.0,
            )

            coEvery {
                dao.getPending()
            } returns listOf(entityProgress, entityRating)

            coJustRun {
                booksRemoteDataSource.replayUpdateBookProgress(
                    userBookReadId = any(),
                    editionId = any(),
                    progressPages = any(),
                    progressSeconds = any(),
                    startedAt = any(),
                    finishedAt = any(),
                )
            }

            coJustRun {
                booksRemoteDataSource.replayUpdateBookRating(
                    userBookId = any(),
                    rating = any(),
                )
            }

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            val result = syncer.drainPendingUpdates()

            // ----- Assert -----
            result shouldBe mapOf(
                userBookId to setOf(
                    PendingUserBookWriteKind.UPDATE_PROGRESS,
                    PendingUserBookWriteKind.UPDATE_RATING,
                ),
            )
        }

        @Test
        fun `discarded row with unknown kind is not included as a key in the returned map`() = runTest {
            // ----- Arrange -----
            val entity = PendingUserBookWriteEntity(
                localId = 30L,
                kind = "OBSOLETE_KIND",
                userBookId = 88,
                userBookReadId = 0,
                bookId = 0,
                editionId = null,
                progressPages = null,
                progressSeconds = null,
                startedAt = null,
                finishedAt = null,
                enqueuedAt = "2026-01-15T09:00:00Z",
            )

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            val result = syncer.drainPendingUpdates()

            // ----- Assert -----
            result shouldBe emptyMap()
            coVerify(exactly = 1) { dao.delete(30L) }
        }
    }
}
