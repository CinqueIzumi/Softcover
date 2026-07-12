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
import nl.rhaydus.softcover.core.connectivity.data.mapper.toPendingListWrite
import nl.rhaydus.softcover.core.database.dao.PendingListWriteDao
import nl.rhaydus.softcover.core.database.model.PendingListWriteEntity
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWriteKind

class PendingListWriteStoreTest {
    private lateinit var dao: PendingListWriteDao
    private lateinit var store: PendingListWriteStore

    @BeforeEach
    fun setUp() {
        dao = mockk()
        store = PendingListWriteStore(dao = dao)
    }

    private fun pendingListWrite(
        kind: PendingListWriteKind = PendingListWriteKind.CREATE_LIST,
        listId: Int? = 10,
        listName: String? = "Science Fiction",
        bookId: Int? = 20,
        editionId: Int? = 30,
        listBookId: Int? = 40,
        startPosition: Int? = 2,
        orderedListBookIds: List<Int>? = listOf(101, 102, 103),
        enqueuedAt: String = "2026-05-10T10:00:00Z",
    ) = PendingListWrite(
        kind = kind,
        listId = listId,
        listName = listName,
        bookId = bookId,
        editionId = editionId,
        listBookId = listBookId,
        startPosition = startPosition,
        orderedListBookIds = orderedListBookIds,
        enqueuedAt = enqueuedAt,
    )

    private fun pendingListWriteEntity(
        localId: Long = 1L,
        kind: String = PendingListWriteKind.CREATE_LIST.name,
        listId: Int? = 10,
        listName: String? = "Science Fiction",
        bookId: Int? = 20,
        editionId: Int? = 30,
        listBookId: Int? = 40,
        startPosition: Int? = 2,
        orderedListBookIdsCsv: String? = "101,102,103",
        enqueuedAt: String = "2026-05-10T10:00:00Z",
        attempts: Int = 0,
    ) = PendingListWriteEntity(
        localId = localId,
        kind = kind,
        listId = listId,
        listName = listName,
        bookId = bookId,
        editionId = editionId,
        listBookId = listBookId,
        startPosition = startPosition,
        orderedListBookIdsCsv = orderedListBookIdsCsv,
        enqueuedAt = enqueuedAt,
        attempts = attempts,
    )

    @Nested
    inner class Enqueue {
        @Test
        fun `maps the payload to an entity and calls insert with every field copied`() = runTest {
            // ----- Arrange -----
            val payload = pendingListWrite(
                kind = PendingListWriteKind.REORDER_LIST_BOOKS,
                listId = 11,
                listName = null,
                bookId = null,
                editionId = null,
                listBookId = null,
                startPosition = 3,
                orderedListBookIds = listOf(201, 202),
                enqueuedAt = "2026-05-11T10:00:00Z",
            )
            val slot = slot<PendingListWriteEntity>()

            coJustRun {
                dao.insert(capture(slot))
            }

            // ----- Act -----
            store.enqueue(payload = payload)

            // ----- Assert -----
            val inserted = slot.captured

            inserted.kind shouldBe "REORDER_LIST_BOOKS"
            inserted.listId shouldBe 11
            inserted.listName shouldBe null
            inserted.bookId shouldBe null
            inserted.editionId shouldBe null
            inserted.listBookId shouldBe null
            inserted.startPosition shouldBe 3
            inserted.orderedListBookIdsCsv shouldBe "201,202"
            inserted.enqueuedAt shouldBe "2026-05-11T10:00:00Z"
        }

        @Test
        fun `round-trips orderedListBookIds through the CSV column`() = runTest {
            // ----- Arrange -----
            val payload = pendingListWrite(orderedListBookIds = listOf(7, 8, 9))
            val slot = slot<PendingListWriteEntity>()

            coJustRun {
                dao.insert(capture(slot))
            }

            // ----- Act -----
            store.enqueue(payload = payload)

            // ----- Assert -----
            val roundTripped = slot.captured.toPendingListWrite()

            roundTripped?.orderedListBookIds shouldBe listOf(7, 8, 9)
        }
    }

    @Nested
    inner class GetPending {
        @Test
        fun `passes maxAttempts through to the dao and maps rows to PendingWrite`() = runTest {
            // ----- Arrange -----
            val entityA = pendingListWriteEntity(
                localId = 1L,
                kind = PendingListWriteKind.CREATE_LIST.name,
                attempts = 0,
            )
            val entityB = pendingListWriteEntity(
                localId = 2L,
                kind = PendingListWriteKind.ADD_LIST_BOOK.name,
                attempts = 3,
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
                    payload = requireNotNull(entityA.toPendingListWrite()),
                ),
                PendingWrite(
                    localId = 2L,
                    attempts = 3,
                    payload = requireNotNull(entityB.toPendingListWrite()),
                ),
            )
            coVerify(exactly = 1) {
                dao.getPending(maxAttempts = 7)
            }
        }

        @Test
        fun `deletes and excludes a row whose kind matches no known PendingListWriteKind`() = runTest {
            // ----- Arrange -----
            val unknownEntity = pendingListWriteEntity(
                localId = 6L,
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
                dao.delete(localId = 6L)
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
