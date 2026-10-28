package nl.rhaydus.softcover.core.connectivity.data.store

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.Called
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import nl.rhaydus.offlinesync.PendingWrite
import nl.rhaydus.softcover.core.book.data.datasource.ProgressHistoryLocalDataSource
import nl.rhaydus.softcover.core.connectivity.data.mapper.toPendingUserBookWrite
import nl.rhaydus.softcover.core.database.dao.PendingUserBookWriteDao
import nl.rhaydus.softcover.core.database.model.PendingUserBookWriteEntity
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.connectivity.USER_BOOK_WRITE_MAX_ATTEMPTS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PendingUserBookWriteStoreTest {
    private lateinit var dao: PendingUserBookWriteDao
    private lateinit var progressHistoryLocalDataSource: ProgressHistoryLocalDataSource
    private lateinit var store: PendingUserBookWriteStore

    @BeforeEach
    fun setUp() {
        dao = mockk()
        progressHistoryLocalDataSource = mockk(relaxed = true)
        store = PendingUserBookWriteStore(
            dao = dao,
            progressHistoryLocalDataSource = progressHistoryLocalDataSource,
        )
    }

    private fun pendingUserBookWrite(
        kind: PendingUserBookWriteKind = PendingUserBookWriteKind.UPDATE_PROGRESS,
        userBookId: Int = 10,
        userBookReadId: Int = 100,
        bookId: Int = 20,
        editionId: Int? = 200,
        progressPages: Int? = 50,
        progressSeconds: Int? = 3600,
        startedAt: String? = "2026-01-01",
        finishedAt: String? = null,
        rating: Double? = 4.5,
        reviewSlateJson: String? = "{\"paragraphs\":[]}",
        reviewHasSpoilers: Boolean? = true,
        enqueuedAt: String = "2026-05-04T12:00:00Z",
        actionAt: String? = null,
        progressHistoryId: Long? = null,
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
    )

    private fun pendingUserBookWriteEntity(
        localId: Long = 1L,
        kind: String = PendingUserBookWriteKind.UPDATE_PROGRESS.name,
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
        enqueuedAt: String = "2026-05-04T12:00:00Z",
        attempts: Int = 0,
        actionAt: String? = null,
        progressHistoryId: Long? = null,
    ) = PendingUserBookWriteEntity(
        localId = localId,
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
        attempts = attempts,
        actionAt = actionAt,
        progressHistoryId = progressHistoryId,
    )

    @Nested
    inner class Enqueue {
        @Test
        fun `maps the payload to an entity and calls insertReplacing with every field copied`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_REVIEW,
                userBookId = 11,
                userBookReadId = 101,
                bookId = 21,
                editionId = 201,
                progressPages = 51,
                progressSeconds = 3601,
                startedAt = "2026-01-02",
                finishedAt = "2026-01-10",
                rating = 3.5,
                reviewSlateJson = "{\"paragraphs\":[\"hi\"]}",
                reviewHasSpoilers = false,
                enqueuedAt = "2026-05-05T09:00:00Z",
                actionAt = "2026-05-05T09:05:00Z",
            )
            val slot = slot<PendingUserBookWriteEntity>()

            coJustRun {
                dao.insertReplacing(capture(slot))
            }

            // ----- Act -----
            store.enqueue(payload = payload)

            // ----- Assert -----
            val inserted = slot.captured

            inserted.kind shouldBe "UPDATE_REVIEW"
            inserted.userBookId shouldBe 11
            inserted.userBookReadId shouldBe 101
            inserted.bookId shouldBe 21
            inserted.editionId shouldBe 201
            inserted.progressPages shouldBe 51
            inserted.progressSeconds shouldBe 3601
            inserted.startedAt shouldBe "2026-01-02"
            inserted.finishedAt shouldBe "2026-01-10"
            inserted.rating shouldBe 3.5
            inserted.reviewSlateJson shouldBe "{\"paragraphs\":[\"hi\"]}"
            inserted.reviewHasSpoilers shouldBe false
            inserted.enqueuedAt shouldBe "2026-05-05T09:00:00Z"
            inserted.actionAt shouldBe "2026-05-05T09:05:00Z"
        }

        @Test
        fun `persists the progressHistoryId on the inserted entity`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(progressHistoryId = 31L)
            val slot = slot<PendingUserBookWriteEntity>()

            coEvery {
                dao.getByUserBookIdAndKind(
                    userBookId = any(),
                    kind = any(),
                )
            } returns null

            coJustRun {
                dao.insertReplacing(capture(slot))
            }

            // ----- Act -----
            store.enqueue(payload = payload)

            // ----- Assert -----
            slot.captured.progressHistoryId shouldBe 31L
        }

        @Test
        fun `deletes the history row of the queued progress write it supersedes`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                userBookId = 10,
                progressHistoryId = 32L,
            )

            coEvery {
                dao.getByUserBookIdAndKind(
                    userBookId = 10,
                    kind = "UPDATE_PROGRESS",
                )
            } returns pendingUserBookWriteEntity(progressHistoryId = 31L)

            coJustRun {
                dao.insertReplacing(any())
            }

            // ----- Act -----
            store.enqueue(payload = payload)

            // ----- Assert -----
            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.delete(localId = 31L)
            }

            coVerify(exactly = 1) {
                dao.insertReplacing(entity = any())
            }
        }

        @Test
        fun `deletes no history row when the superseded progress write has none`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(kind = PendingUserBookWriteKind.UPDATE_PROGRESS)

            coEvery {
                dao.getByUserBookIdAndKind(
                    userBookId = any(),
                    kind = any(),
                )
            } returns pendingUserBookWriteEntity(progressHistoryId = null)

            coJustRun {
                dao.insertReplacing(any())
            }

            // ----- Act -----
            store.enqueue(payload = payload)

            // ----- Assert -----
            verify { progressHistoryLocalDataSource wasNot Called }
        }

        @Test
        fun `deletes no history row when no progress write is queued yet`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                progressHistoryId = 32L,
            )

            coEvery {
                dao.getByUserBookIdAndKind(
                    userBookId = any(),
                    kind = any(),
                )
            } returns null

            coJustRun {
                dao.insertReplacing(any())
            }

            // ----- Act -----
            store.enqueue(payload = payload)

            // ----- Assert -----
            verify { progressHistoryLocalDataSource wasNot Called }

            coVerify(exactly = 1) {
                dao.insertReplacing(entity = any())
            }
        }

        @Test
        fun `leaves history alone for every kind other than UPDATE_PROGRESS`() = runTest {
            // ----- Arrange -----
            val otherKinds = PendingUserBookWriteKind.entries - PendingUserBookWriteKind.UPDATE_PROGRESS

            coJustRun {
                dao.insertReplacing(any())
            }

            // ----- Act -----
            otherKinds.forEach { kind ->
                store.enqueue(payload = pendingUserBookWrite(kind = kind))
            }

            // ----- Assert -----
            verify { progressHistoryLocalDataSource wasNot Called }

            coVerify(exactly = otherKinds.size) {
                dao.insertReplacing(entity = any())
            }

            coVerify(exactly = 0) {
                dao.getByUserBookIdAndKind(
                    userBookId = any(),
                    kind = any(),
                )
            }
        }
    }

    @Nested
    inner class GetPending {
        @Test
        fun `maps dao rows to PendingWrite`() = runTest {
            // ----- Arrange -----
            val entityA = pendingUserBookWriteEntity(
                localId = 1L,
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS.name,
                attempts = 0,
                actionAt = "2026-06-01T10:00:00Z",
            )
            val entityB = pendingUserBookWriteEntity(
                localId = 2L,
                kind = PendingUserBookWriteKind.MARK_AS_READ.name,
                attempts = 2,
            )

            coEvery {
                dao.getPending(maxAttempts = 7)
            } returns listOf(entityA, entityB)

            // ----- Act -----
            val result = store.getPending(maxAttempts = 7)

            // ----- Assert -----
            result shouldBe listOf(
                PendingWrite(
                    localId = 1L,
                    attempts = 0,
                    payload = requireNotNull(entityA.toPendingUserBookWrite()),
                ),
                PendingWrite(
                    localId = 2L,
                    attempts = 2,
                    payload = requireNotNull(entityB.toPendingUserBookWrite()),
                ),
            )
            result[0].payload.actionAt shouldBe "2026-06-01T10:00:00Z"
            result[1].payload.actionAt shouldBe null
        }

        @Test
        fun `deletes and excludes a row whose kind matches no known PendingUserBookWriteKind`() = runTest {
            // ----- Arrange -----
            val unknownEntity = pendingUserBookWriteEntity(
                localId = 5L,
                kind = "SOME_FUTURE_KIND",
            )

            coEvery {
                dao.getPending(maxAttempts = any())
            } returns listOf(unknownEntity)

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            val result = store.getPending(maxAttempts = 5)

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 1) {
                dao.delete(localId = 5L)
            }
        }
    }

    @Nested
    inner class HasPending {
        @Test
        fun `returns true when the dao has an eligible pending row`() = runTest {
            // ----- Arrange -----
            coEvery {
                dao.getPending(maxAttempts = any())
            } returns listOf(pendingUserBookWriteEntity())

            // ----- Act -----
            val result = store.hasPending()

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `returns false when the dao has no eligible pending rows`() = runTest {
            // ----- Arrange -----
            coEvery {
                dao.getPending(maxAttempts = any())
            } returns emptyList()

            // ----- Act -----
            val result = store.hasPending()

            // ----- Assert -----
            result shouldBe false
        }
    }

    @Nested
    inner class Append {
        @Test
        fun `returns the row id the dao assigned`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(kind = PendingUserBookWriteKind.UPDATE_RATING)

            coEvery {
                dao.insertReplacing(entity = any())
            } returns 77L

            // ----- Act -----
            val result = store.append(write = payload)

            // ----- Assert -----
            result shouldBe 77L
        }

        @Test
        fun `deletes the history row of the superseded progress write`() = runTest {
            // ----- Arrange -----
            val payload = pendingUserBookWrite(
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                progressHistoryId = 32L,
            )

            coEvery {
                dao.getByUserBookIdAndKind(
                    userBookId = 10,
                    kind = "UPDATE_PROGRESS",
                )
            } returns pendingUserBookWriteEntity(progressHistoryId = 31L)

            coEvery {
                dao.insertReplacing(entity = any())
            } returns 5L

            // ----- Act -----
            store.append(write = payload)

            // ----- Assert -----
            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.delete(localId = 31L)
            }
        }
    }

    @Nested
    inner class Remove {
        @Test
        fun `deletes only the given localId`() = runTest {
            // ----- Arrange -----
            coJustRun {
                dao.delete(localId = any())
            }

            // ----- Act -----
            store.remove(localId = 9L)

            // ----- Assert -----
            coVerify(exactly = 1) {
                dao.delete(localId = 9L)
            }

            coVerify(exactly = 1) {
                dao.delete(localId = any())
            }
        }
    }

    @Nested
    inner class IsCurrent {
        @Test
        fun `returns true when the stored row equals the write`() = runTest {
            // ----- Arrange -----
            val entity = pendingUserBookWriteEntity()
            val write = entity.toPendingUserBookWrite().shouldNotBeNull()

            coEvery {
                dao.getByUserBookIdAndKind(
                    userBookId = write.userBookId,
                    kind = write.kind.name,
                )
            } returns entity

            // ----- Act -----
            val result = store.isCurrent(write = write)

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `returns false when the stored row differs from the write`() = runTest {
            // ----- Arrange -----
            val write = pendingUserBookWriteEntity(progressPages = 50)
                .toPendingUserBookWrite()
                .shouldNotBeNull()

            coEvery {
                dao.getByUserBookIdAndKind(
                    userBookId = any(),
                    kind = any(),
                )
            } returns pendingUserBookWriteEntity(progressPages = 60)

            // ----- Act -----
            val result = store.isCurrent(write = write)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `returns false when the stored row is gone`() = runTest {
            // ----- Arrange -----
            val write = pendingUserBookWrite()

            coEvery {
                dao.getByUserBookIdAndKind(
                    userBookId = any(),
                    kind = any(),
                )
            } returns null

            // ----- Act -----
            val result = store.isCurrent(write = write)

            // ----- Assert -----
            result shouldBe false
        }
    }

    @Nested
    inner class PendingKindsByUserBook {
        @Test
        fun `groups the pending kinds per user book`() = runTest {
            // ----- Arrange -----
            coEvery {
                dao.getPending(maxAttempts = any())
            } returns listOf(
                pendingUserBookWriteEntity(
                    localId = 1L,
                    userBookId = 10,
                    kind = PendingUserBookWriteKind.UPDATE_PROGRESS.name,
                ),
                pendingUserBookWriteEntity(
                    localId = 2L,
                    userBookId = 10,
                    kind = PendingUserBookWriteKind.UPDATE_RATING.name,
                ),
                pendingUserBookWriteEntity(
                    localId = 3L,
                    userBookId = 11,
                    kind = PendingUserBookWriteKind.UPDATE_PROGRESS.name,
                ),
            )

            // ----- Act -----
            val result = store.pendingKindsByUserBook()

            // ----- Assert -----
            result shouldBe mapOf(
                10 to setOf(
                    PendingUserBookWriteKind.UPDATE_PROGRESS,
                    PendingUserBookWriteKind.UPDATE_RATING,
                ),
                11 to setOf(PendingUserBookWriteKind.UPDATE_PROGRESS),
            )
        }

        @Test
        fun `queries the dao with the attempt cap`() = runTest {
            // ----- Arrange -----
            coEvery {
                dao.getPending(maxAttempts = any())
            } returns emptyList()

            // ----- Act -----
            store.pendingKindsByUserBook()

            // ----- Assert -----
            coVerify(exactly = 1) {
                dao.getPending(maxAttempts = USER_BOOK_WRITE_MAX_ATTEMPTS)
            }
        }
    }

    @Nested
    inner class HasOlderPending {
        @Test
        fun `asks the dao with the cap and the excluded localId`() = runTest {
            // ----- Arrange -----
            coEvery {
                dao.hasOlderPending(
                    bookId = any(),
                    exceptLocalId = any(),
                    maxAttempts = any(),
                )
            } returns true

            // ----- Act -----
            val result = store.hasOlderPending(
                bookId = 20,
                exceptLocalId = 4L,
            )

            // ----- Assert -----
            result shouldBe true

            coVerify(exactly = 1) {
                dao.hasOlderPending(
                    bookId = 20,
                    exceptLocalId = 4L,
                    maxAttempts = USER_BOOK_WRITE_MAX_ATTEMPTS,
                )
            }
        }
    }
}
