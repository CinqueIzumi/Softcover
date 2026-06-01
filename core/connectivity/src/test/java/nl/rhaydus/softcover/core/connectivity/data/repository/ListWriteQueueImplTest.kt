package nl.rhaydus.softcover.core.connectivity.data.repository

import io.kotest.matchers.shouldBe
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.data.database.dao.PendingListWriteDao
import nl.rhaydus.softcover.core.data.database.model.PendingListWriteEntity
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWriteKind
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListWriteQueueImplTest {

    private lateinit var dao: PendingListWriteDao
    private lateinit var queue: ListWriteQueueImpl

    @BeforeEach
    fun setUp() {
        dao = mockk()
        queue = ListWriteQueueImpl(dao = dao)
    }

    private fun pendingListWrite(
        kind: PendingListWriteKind,
        listId: Int? = null,
        listName: String? = null,
        bookId: Int? = null,
        editionId: Int? = null,
        listBookId: Int? = null,
        startPosition: Int? = null,
        orderedListBookIds: List<Int>? = null,
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

    @Nested
    inner class CreateList {

        @Test
        fun `serializes CREATE_LIST with listName and null id fields`() = runTest {
            // ----- Arrange -----
            val write = pendingListWrite(
                kind = PendingListWriteKind.CREATE_LIST,
                listName = "Science Fiction",
                enqueuedAt = "2026-05-10T10:00:00Z",
            )
            val slot = slot<PendingListWriteEntity>()

            coJustRun {
                dao.insert(capture(slot))
            }

            // ----- Act -----
            queue.enqueue(write = write)

            // ----- Assert -----
            val inserted = slot.captured

            inserted.kind shouldBe "CREATE_LIST"
            inserted.listName shouldBe "Science Fiction"
            inserted.listId shouldBe null
            inserted.bookId shouldBe null
            inserted.editionId shouldBe null
            inserted.listBookId shouldBe null
            inserted.startPosition shouldBe null
            inserted.orderedListBookIdsCsv shouldBe null
            inserted.enqueuedAt shouldBe "2026-05-10T10:00:00Z"
        }
    }

    @Nested
    inner class AddListBook {

        @Test
        fun `serializes ADD_LIST_BOOK with listId, bookId, editionId and null name and csv fields`() = runTest {
            // ----- Arrange -----
            val write = pendingListWrite(
                kind = PendingListWriteKind.ADD_LIST_BOOK,
                listId = 10,
                bookId = 20,
                editionId = 30,
                enqueuedAt = "2026-05-10T11:00:00Z",
            )
            val slot = slot<PendingListWriteEntity>()

            coJustRun {
                dao.insert(capture(slot))
            }

            // ----- Act -----
            queue.enqueue(write = write)

            // ----- Assert -----
            val inserted = slot.captured

            inserted.kind shouldBe "ADD_LIST_BOOK"
            inserted.listId shouldBe 10
            inserted.bookId shouldBe 20
            inserted.editionId shouldBe 30
            inserted.listName shouldBe null
            inserted.listBookId shouldBe null
            inserted.startPosition shouldBe null
            inserted.orderedListBookIdsCsv shouldBe null
            inserted.enqueuedAt shouldBe "2026-05-10T11:00:00Z"
        }
    }

    @Nested
    inner class RemoveListBook {

        @Test
        fun `serializes REMOVE_LIST_BOOK with listId, bookId, editionId, listBookId and null csv`() = runTest {
            // ----- Arrange -----
            val write = pendingListWrite(
                kind = PendingListWriteKind.REMOVE_LIST_BOOK,
                listId = 10,
                bookId = 20,
                editionId = 30,
                listBookId = 40,
                enqueuedAt = "2026-05-10T12:00:00Z",
            )
            val slot = slot<PendingListWriteEntity>()

            coJustRun {
                dao.insert(capture(slot))
            }

            // ----- Act -----
            queue.enqueue(write = write)

            // ----- Assert -----
            val inserted = slot.captured

            inserted.kind shouldBe "REMOVE_LIST_BOOK"
            inserted.listId shouldBe 10
            inserted.bookId shouldBe 20
            inserted.editionId shouldBe 30
            inserted.listBookId shouldBe 40
            inserted.listName shouldBe null
            inserted.startPosition shouldBe null
            inserted.orderedListBookIdsCsv shouldBe null
            inserted.enqueuedAt shouldBe "2026-05-10T12:00:00Z"
        }
    }

    @Nested
    inner class ReorderListBooks {

        @Test
        fun `serializes REORDER_LIST_BOOKS — joins orderedListBookIds as CSV`() = runTest {
            // ----- Arrange -----
            val write = pendingListWrite(
                kind = PendingListWriteKind.REORDER_LIST_BOOKS,
                listId = 10,
                startPosition = 2,
                orderedListBookIds = listOf(101, 102, 103),
                enqueuedAt = "2026-05-10T13:00:00Z",
            )
            val slot = slot<PendingListWriteEntity>()

            coJustRun {
                dao.insert(capture(slot))
            }

            // ----- Act -----
            queue.enqueue(write = write)

            // ----- Assert -----
            val inserted = slot.captured

            inserted.kind shouldBe "REORDER_LIST_BOOKS"
            inserted.listId shouldBe 10
            inserted.startPosition shouldBe 2
            inserted.orderedListBookIdsCsv shouldBe "101,102,103"
            inserted.listName shouldBe null
            inserted.bookId shouldBe null
            inserted.editionId shouldBe null
            inserted.listBookId shouldBe null
            inserted.enqueuedAt shouldBe "2026-05-10T13:00:00Z"
        }

        @Test
        fun `null orderedListBookIds is stored as null CSV`() = runTest {
            // ----- Arrange -----
            val write = pendingListWrite(
                kind = PendingListWriteKind.REORDER_LIST_BOOKS,
                listId = 10,
                startPosition = 0,
                orderedListBookIds = null,
                enqueuedAt = "2026-05-10T14:00:00Z",
            )
            val slot = slot<PendingListWriteEntity>()

            coJustRun {
                dao.insert(capture(slot))
            }

            // ----- Act -----
            queue.enqueue(write = write)

            // ----- Assert -----
            slot.captured.orderedListBookIdsCsv shouldBe null
        }

        @Test
        fun `single-element list produces CSV with no separator`() = runTest {
            // ----- Arrange -----
            val write = pendingListWrite(
                kind = PendingListWriteKind.REORDER_LIST_BOOKS,
                listId = 5,
                startPosition = 0,
                orderedListBookIds = listOf(42),
                enqueuedAt = "2026-05-10T15:00:00Z",
            )
            val slot = slot<PendingListWriteEntity>()

            coJustRun {
                dao.insert(capture(slot))
            }

            // ----- Act -----
            queue.enqueue(write = write)

            // ----- Assert -----
            slot.captured.orderedListBookIdsCsv shouldBe "42"
        }

        @Test
        fun `delegates to dao insert exactly once`() = runTest {
            // ----- Arrange -----
            val write = pendingListWrite(
                kind = PendingListWriteKind.REORDER_LIST_BOOKS,
                listId = 10,
                startPosition = 1,
                orderedListBookIds = listOf(5, 6),
            )

            coJustRun {
                dao.insert(any())
            }

            // ----- Act -----
            queue.enqueue(write = write)

            // ----- Assert -----
            coVerify(exactly = 1) {
                dao.insert(entity = any())
            }
        }
    }
}
