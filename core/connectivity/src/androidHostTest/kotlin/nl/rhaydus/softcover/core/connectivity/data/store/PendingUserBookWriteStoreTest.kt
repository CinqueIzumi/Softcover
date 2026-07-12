package nl.rhaydus.softcover.core.connectivity.data.store

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.offlinesync.PendingWrite
import nl.rhaydus.softcover.core.connectivity.data.mapper.toPendingUserBookWrite
import nl.rhaydus.softcover.core.database.dao.PendingUserBookWriteDao
import nl.rhaydus.softcover.core.database.model.PendingUserBookWriteEntity
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind

class PendingUserBookWriteStoreTest {
    private lateinit var dao: PendingUserBookWriteDao
    private lateinit var store: PendingUserBookWriteStore

    @BeforeEach
    fun setUp() {
        dao = mockk()
        store = PendingUserBookWriteStore(dao = dao)
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
        }
    }

    @Nested
    inner class GetPending {
        @Test
        fun `passes maxAttempts through to the dao and maps rows to PendingWrite`() = runTest {
            // ----- Arrange -----
            val entityA = pendingUserBookWriteEntity(
                localId = 1L,
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS.name,
                attempts = 0,
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
            coVerify(exactly = 1) {
                dao.getPending(maxAttempts = 7)
            }
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
    inner class Delete {
        @Test
        fun `delegates to dao delete`() = runTest {
            // ----- Arrange -----
            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            store.delete(localId = 9L)

            // ----- Assert -----
            coVerify(exactly = 1) {
                dao.delete(localId = 9L)
            }
        }
    }

    @Nested
    inner class IncrementAttempts {
        @Test
        fun `delegates to dao incrementAttempts`() = runTest {
            // ----- Arrange -----
            coJustRun {
                dao.incrementAttempts(any())
            }

            // ----- Act -----
            store.incrementAttempts(localId = 9L)

            // ----- Assert -----
            coVerify(exactly = 1) {
                dao.incrementAttempts(localId = 9L)
            }
        }
    }
}
