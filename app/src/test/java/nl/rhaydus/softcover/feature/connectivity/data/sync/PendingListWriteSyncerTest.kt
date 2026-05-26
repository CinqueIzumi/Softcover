package nl.rhaydus.softcover.feature.connectivity.data.sync

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWriteKind
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.feature.connectivity.data.dao.PendingListWriteDao
import nl.rhaydus.softcover.feature.connectivity.data.model.PendingListWriteEntity
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsRemoteDataSource
import kotlin.coroutines.cancellation.CancellationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PendingListWriteSyncerTest {

    private lateinit var networkAvailability: NetworkAvailabilityProvider
    private lateinit var dao: PendingListWriteDao
    private lateinit var listsRemoteDataSource: ListsRemoteDataSource
    private lateinit var listsLocalDataSource: ListsLocalDataSource
    private lateinit var syncer: PendingListWriteSyncer

    @BeforeEach
    fun setUp() {
        networkAvailability = mockk()
        dao = mockk()
        listsRemoteDataSource = mockk()
        listsLocalDataSource = mockk(relaxed = true)

        syncer = PendingListWriteSyncer(
            networkAvailability = networkAvailability,
            dao = dao,
            listsRemoteDataSource = listsRemoteDataSource,
            listsLocalDataSource = listsLocalDataSource,
        )
    }

    private fun createListEntity(
        localId: Long = 1L,
        listName: String = "My List",
        enqueuedAt: String = "2026-05-10T10:00:00Z",
    ) = PendingListWriteEntity(
        localId = localId,
        kind = PendingListWriteKind.CREATE_LIST.name,
        listId = null,
        listName = listName,
        bookId = null,
        editionId = null,
        listBookId = null,
        startPosition = null,
        orderedListBookIdsCsv = null,
        enqueuedAt = enqueuedAt,
    )

    private fun addListBookEntity(
        localId: Long = 2L,
        listId: Int = 10,
        bookId: Int = 20,
        editionId: Int = 30,
        enqueuedAt: String = "2026-05-10T10:01:00Z",
    ) = PendingListWriteEntity(
        localId = localId,
        kind = PendingListWriteKind.ADD_LIST_BOOK.name,
        listId = listId,
        listName = null,
        bookId = bookId,
        editionId = editionId,
        listBookId = null,
        startPosition = null,
        orderedListBookIdsCsv = null,
        enqueuedAt = enqueuedAt,
    )

    private fun removeListBookEntity(
        localId: Long = 3L,
        listId: Int = 10,
        listBookId: Int = 40,
        bookId: Int = 20,
        editionId: Int = 30,
        enqueuedAt: String = "2026-05-10T10:02:00Z",
    ) = PendingListWriteEntity(
        localId = localId,
        kind = PendingListWriteKind.REMOVE_LIST_BOOK.name,
        listId = listId,
        listName = null,
        bookId = bookId,
        editionId = editionId,
        listBookId = listBookId,
        startPosition = null,
        orderedListBookIdsCsv = null,
        enqueuedAt = enqueuedAt,
    )

    private fun reorderListBooksEntity(
        localId: Long = 4L,
        listId: Int = 10,
        startPosition: Int = 2,
        orderedListBookIdsCsv: String = "101,102,103",
        enqueuedAt: String = "2026-05-10T10:03:00Z",
    ) = PendingListWriteEntity(
        localId = localId,
        kind = PendingListWriteKind.REORDER_LIST_BOOKS.name,
        listId = listId,
        listName = null,
        bookId = null,
        editionId = null,
        listBookId = null,
        startPosition = startPosition,
        orderedListBookIdsCsv = orderedListBookIdsCsv,
        enqueuedAt = enqueuedAt,
    )

    private fun stubBookList(id: Int = 1, name: String = "My List"): BookList = BookList(
        id = id,
        name = name,
        slug = name.lowercase().replace(" ", "-"),
        books = emptyList(),
    )

    private fun stubListBook(
        listBookId: Int = 99,
        listId: Int = 10,
        bookId: Int = 20,
        editionId: Int = 30,
    ): ListBook = ListBook(
        listBookId = listBookId,
        listId = listId,
        bookId = bookId,
        editionId = editionId,
    )

    @Nested
    inner class DrainPendingWrites {

        @Test
        fun `empty queue is a no-op — no remote or local calls`() = runTest {
            // ----- Arrange -----
            coEvery {
                dao.getPending()
            } returns emptyList()

            // ----- Act -----
            syncer.drainPendingWrites()

            // ----- Assert -----
            coVerify(exactly = 0) {
                listsRemoteDataSource.createList(name = any())
            }

            coVerify(exactly = 0) {
                listsRemoteDataSource.addBookToList(
                    listId = any(),
                    bookId = any(),
                    editionId = any(),
                )
            }

            coVerify(exactly = 0) {
                listsRemoteDataSource.removeListBook(book = any())
            }

            coVerify(exactly = 0) {
                listsRemoteDataSource.updateListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            }

            coVerify(exactly = 0) {
                dao.delete(localId = any())
            }
        }

        @Test
        fun `FIFO drain success — deletes each entry in order`() = runTest {
            // ----- Arrange -----
            val entityA = createListEntity(localId = 1L)
            val entityB = reorderListBooksEntity(localId = 2L)
            val createdList = stubBookList()
            val orderedIds = listOf(101, 102, 103)

            coEvery {
                dao.getPending()
            } returns listOf(entityA, entityB)

            coEvery {
                listsRemoteDataSource.createList(name = "My List")
            } returns createdList

            coJustRun {
                listsRemoteDataSource.updateListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            }

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            syncer.drainPendingWrites()

            // ----- Assert -----
            coVerify(exactly = 1) { dao.delete(1L) }
            coVerify(exactly = 1) { dao.delete(2L) }
        }

        @Test
        fun `replay failure halts drain — later entries are not replayed and attempts is incremented`() = runTest {
            // ----- Arrange -----
            val entityA = createListEntity(localId = 1L)
            val entityB = addListBookEntity(localId = 2L, listId = 10, bookId = 20, editionId = 30)
            val entityC = reorderListBooksEntity(localId = 3L)

            coEvery {
                dao.getPending()
            } returns listOf(entityA, entityB, entityC)

            coEvery {
                listsRemoteDataSource.createList(name = any())
            } throws RuntimeException("network error")

            coJustRun {
                dao.incrementAttempts(any())
            }

            // ----- Act -----
            syncer.drainPendingWrites()

            // ----- Assert -----
            coVerify(exactly = 1) { dao.incrementAttempts(1L) }

            coVerify(exactly = 0) { dao.delete(any()) }

            coVerify(exactly = 0) {
                listsRemoteDataSource.addBookToList(
                    listId = any(),
                    bookId = any(),
                    editionId = any(),
                )
            }

            coVerify(exactly = 0) {
                listsRemoteDataSource.updateListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            }
        }

        @Test
        fun `in-drain exponential backoff retries up to 3 times before halting`() = runTest {
            // ----- Arrange -----
            val entity = createListEntity(localId = 5L)

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coEvery {
                listsRemoteDataSource.createList(name = any())
            } throws RuntimeException("always fails")

            coJustRun {
                dao.incrementAttempts(any())
            }

            // ----- Act -----
            syncer.drainPendingWrites()

            // ----- Assert -----
            coVerify(exactly = 3) {
                listsRemoteDataSource.createList(name = any())
            }

            coVerify(exactly = 1) { dao.incrementAttempts(5L) }

            coVerify(exactly = 0) { dao.delete(any()) }
        }

        @Test
        fun `in-drain retry succeeds on second attempt — entry is deleted`() = runTest {
            // ----- Arrange -----
            val entity = createListEntity(localId = 7L)
            val createdList = stubBookList()

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coEvery {
                listsRemoteDataSource.createList(name = any())
            } throws RuntimeException("transient") andThen createdList

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            syncer.drainPendingWrites()

            // ----- Assert -----
            coVerify(exactly = 2) {
                listsRemoteDataSource.createList(name = any())
            }

            coVerify(exactly = 1) { dao.delete(7L) }

            coVerify(exactly = 0) { dao.incrementAttempts(any()) }
        }

        @Test
        fun `CancellationException propagates and does not increment attempts`() = runTest {
            // ----- Arrange -----
            val entity = createListEntity(localId = 9L)

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coEvery {
                listsRemoteDataSource.createList(name = any())
            } throws CancellationException("cancelled")

            // ----- Act & Assert -----
            shouldThrow<CancellationException> {
                syncer.drainPendingWrites()
            }

            coVerify(exactly = 0) { dao.incrementAttempts(any()) }

            coVerify(exactly = 0) { dao.delete(any()) }
        }
    }

    @Nested
    inner class ReplayCreateList {

        @Test
        fun `delegates to remote createList with listName and caches the created BookList`() = runTest {
            // ----- Arrange -----
            val entity = createListEntity(localId = 1L, listName = "Science Fiction")
            val createdList = stubBookList(id = 7, name = "Science Fiction")

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coEvery {
                listsRemoteDataSource.createList(name = "Science Fiction")
            } returns createdList

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            syncer.drainPendingWrites()

            // ----- Assert -----
            coVerify(exactly = 1) {
                listsRemoteDataSource.createList(name = "Science Fiction")
            }

            coVerify(exactly = 1) {
                listsLocalDataSource.cacheUserBookLists(lists = listOf(createdList))
            }

            coVerify(exactly = 1) { dao.delete(1L) }
        }
    }

    @Nested
    inner class ReplayAddListBook {

        @Test
        fun `delegates to remote addBookToList then removes optimistic and caches real ListBook`() = runTest {
            // ----- Arrange -----
            val entity = addListBookEntity(
                localId = 2L,
                listId = 10,
                bookId = 20,
                editionId = 30,
            )
            val realListBook = stubListBook(listBookId = 99, listId = 10, bookId = 20, editionId = 30)

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coEvery {
                listsRemoteDataSource.addBookToList(
                    listId = 10,
                    bookId = 20,
                    editionId = 30,
                )
            } returns realListBook

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            syncer.drainPendingWrites()

            // ----- Assert -----
            coVerify(exactly = 1) {
                listsRemoteDataSource.addBookToList(
                    listId = 10,
                    bookId = 20,
                    editionId = 30,
                )
            }

            coVerify(exactly = 1) {
                listsLocalDataSource.removeOptimisticListBook(
                    listId = 10,
                    bookId = 20,
                )
            }

            coVerify(exactly = 1) {
                listsLocalDataSource.cacheListBook(book = realListBook)
            }

            coVerify(exactly = 1) { dao.delete(2L) }
        }
    }

    @Nested
    inner class ReplayRemoveListBook {

        @Test
        fun `delegates to remote removeListBook with reconstructed ListBook and caches returned BookList`() = runTest {
            // ----- Arrange -----
            val entity = removeListBookEntity(
                localId = 3L,
                listId = 10,
                listBookId = 40,
                bookId = 20,
                editionId = 30,
            )
            val expectedSnapshot = ListBook(
                listBookId = 40,
                listId = 10,
                bookId = 20,
                editionId = 30,
            )
            val updatedList = stubBookList(id = 10)

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coEvery {
                listsRemoteDataSource.removeListBook(book = expectedSnapshot)
            } returns updatedList

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            syncer.drainPendingWrites()

            // ----- Assert -----
            coVerify(exactly = 1) {
                listsRemoteDataSource.removeListBook(book = expectedSnapshot)
            }

            coVerify(exactly = 1) {
                listsLocalDataSource.cacheUserBookLists(lists = listOf(updatedList))
            }

            coVerify(exactly = 1) { dao.delete(3L) }
        }
    }

    @Nested
    inner class ReplayReorderListBooks {

        @Test
        fun `delegates to remote updateListBookPositions — no local writes`() = runTest {
            // ----- Arrange -----
            val entity = reorderListBooksEntity(
                localId = 4L,
                listId = 10,
                startPosition = 2,
                orderedListBookIdsCsv = "101,102,103",
            )

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coJustRun {
                listsRemoteDataSource.updateListBookPositions(
                    listId = 10,
                    startPosition = 2,
                    orderedListBookIds = listOf(101, 102, 103),
                )
            }

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            syncer.drainPendingWrites()

            // ----- Assert -----
            coVerify(exactly = 1) {
                listsRemoteDataSource.updateListBookPositions(
                    listId = 10,
                    startPosition = 2,
                    orderedListBookIds = listOf(101, 102, 103),
                )
            }

            coVerify(exactly = 0) {
                listsLocalDataSource.cacheUserBookLists(lists = any())
            }

            coVerify(exactly = 0) {
                listsLocalDataSource.cacheListBook(book = any())
            }

            coVerify(exactly = 1) { dao.delete(4L) }
        }

        @Test
        fun `CSV is parsed back into integer list and passed to remote`() = runTest {
            // ----- Arrange -----
            val entity = reorderListBooksEntity(
                localId = 6L,
                listId = 55,
                startPosition = 0,
                orderedListBookIdsCsv = " 7 , 3 , 11 ",
            )

            coEvery {
                dao.getPending()
            } returns listOf(entity)

            coJustRun {
                listsRemoteDataSource.updateListBookPositions(
                    listId = 55,
                    startPosition = 0,
                    orderedListBookIds = listOf(7, 3, 11),
                )
            }

            coJustRun {
                dao.delete(any())
            }

            // ----- Act -----
            syncer.drainPendingWrites()

            // ----- Assert -----
            coVerify(exactly = 1) {
                listsRemoteDataSource.updateListBookPositions(
                    listId = 55,
                    startPosition = 0,
                    orderedListBookIds = listOf(7, 3, 11),
                )
            }
        }
    }
}
