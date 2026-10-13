package nl.rhaydus.softcover.core.lists.data.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteQueue
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWriteKind
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.core.lists.data.datasource.ListsRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.coroutines.cancellation.CancellationException

class ListsRepositoryImplAddRemoveListBookTest {
    private lateinit var listsRemoteDataSource: ListsRemoteDataSource
    private lateinit var listsLocalDataSource: ListsLocalDataSource
    private val booksRepository = mockk<BooksRepository>()
    private lateinit var listWriteQueue: ListWriteQueue
    private lateinit var listWriteDrainer: ListWriteDrainer
    private lateinit var repository: ListsRepositoryImpl

    @BeforeEach
    fun setUp() {
        listsRemoteDataSource = mockk()
        listsLocalDataSource = mockk(relaxed = true)
        listWriteQueue = mockk(relaxed = true)
        listWriteDrainer = mockk(relaxed = true)

        coEvery {
            booksRepository.hydrateReferencedBooks(
                bookIds = any(),
                editionIds = any(),
                forceNetwork = any(),
            )
        } returns Unit

        repository = ListsRepositoryImpl(
            listsRemoteDataSource = listsRemoteDataSource,
            listsLocalDataSource = listsLocalDataSource,
            booksRepository = booksRepository,
            applicationScope = ApplicationScope(scope = CoroutineScope(UnconfinedTestDispatcher())),
            listWriteQueue = listWriteQueue,
            listWriteDrainer = listWriteDrainer,
        )
    }

    private fun stubEdition(
        id: Int,
        bookId: Int = 1,
    ): BookEdition = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.bookId
        } returns bookId
    }

    private fun stubListBook(
        listBookId: Int = 1,
        listId: Int = 0,
        bookId: Int = 1,
        editionId: Int = 10,
    ): ListBook = ListBook(
        listBookId = listBookId,
        listId = listId,
        bookId = bookId,
        editionId = editionId,
    )

    private fun stubBookList(id: Int = 1): BookList = BookList(
        id = id,
        name = "my list",
        slug = "my-list",
        books = emptyList(),
    )

    @Nested
    inner class AddBookToList {
        @Test
        fun `caches a placeholder, then swaps it for the real ListBook on remote success`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val editionId = 10
            val edition = stubEdition(
                id = editionId,
                bookId = bookId,
            )
            val realListBook = stubListBook(
                listBookId = 99,
                listId = listId,
                bookId = bookId,
                editionId = editionId,
            )

            coEvery {
                listsRemoteDataSource.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    editionId = editionId,
                )
            } returns realListBook

            // ----- Act -----
            repository.addBookToList(
                listId = listId,
                bookId = bookId,
                edition = edition,
            )

            // ----- Assert -----
            coVerify {
                listsLocalDataSource.cacheListBook(
                    book = ListBook(
                        listBookId = 0,
                        listId = listId,
                        bookId = bookId,
                        editionId = editionId,
                    ),
                )
            }

            coVerify {
                booksRepository.hydrateReferencedBooks(
                    bookIds = listOf(bookId),
                    editionIds = listOf(editionId),
                    forceNetwork = false,
                )
            }

            coVerify {
                listsLocalDataSource.removeOptimisticListBook(
                    listId = listId,
                    bookId = bookId,
                )
            }

            coVerify {
                listsLocalDataSource.cacheListBook(book = realListBook)
            }
        }

        @Test
        fun `on a retryable failure returns normally, keeps the optimistic entry and enqueues ADD_LIST_BOOK`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val editionId = 10
            val edition = stubEdition(
                id = editionId,
                bookId = bookId,
            )

            coEvery {
                listsRemoteDataSource.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    editionId = editionId,
                )
            } throws OfflineException()

            val slot = mutableListOf<PendingListWrite>()

            coJustRun {
                listWriteQueue.enqueue(capture(slot))
            }

            // ----- Act -----
            repository.addBookToList(
                listId = listId,
                bookId = bookId,
                edition = edition,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                listWriteQueue.enqueue(payload = any())
            }

            val enqueued = slot.first()

            enqueued.kind shouldBe PendingListWriteKind.ADD_LIST_BOOK
            enqueued.listId shouldBe listId
            enqueued.bookId shouldBe bookId
            enqueued.editionId shouldBe editionId

            coVerify(exactly = 0) {
                listsLocalDataSource.removeOptimisticListBook(
                    listId = any(),
                    bookId = any(),
                )
            }
        }

        @Test
        fun `on a rejected failure rolls back the optimistic entry, rethrows and does not enqueue`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val editionId = 10
            val edition = stubEdition(
                id = editionId,
                bookId = bookId,
            )
            val remoteError = RuntimeException("rejected")

            coEvery {
                listsRemoteDataSource.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    editionId = editionId,
                )
            } throws remoteError

            // ----- Act -----
            val thrown = shouldThrow<RuntimeException> {
                repository.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    edition = edition,
                )
            }

            // ----- Assert -----
            thrown shouldBe remoteError

            coVerify(exactly = 1) {
                listsLocalDataSource.removeOptimisticListBook(
                    listId = listId,
                    bookId = bookId,
                )
            }

            coVerify(exactly = 0) {
                listWriteQueue.enqueue(payload = any())
            }
        }

        @Test
        fun `when enqueueing a retryable failure also fails, rolls back and rethrows the queue error`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val editionId = 10
            val edition = stubEdition(
                id = editionId,
                bookId = bookId,
            )
            val queueError = IllegalStateException("queue full")

            coEvery {
                listsRemoteDataSource.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    editionId = editionId,
                )
            } throws OfflineException()

            coEvery {
                listWriteQueue.enqueue(payload = any())
            } throws queueError

            // ----- Act -----
            val thrown = shouldThrow<IllegalStateException> {
                repository.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    edition = edition,
                )
            }

            // ----- Assert -----
            thrown shouldBe queueError

            coVerify(exactly = 1) {
                listsLocalDataSource.removeOptimisticListBook(
                    listId = listId,
                    bookId = bookId,
                )
            }
        }

        @Test
        fun `cancellation — rethrows CancellationException without rollback`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val editionId = 10
            val edition = stubEdition(
                id = editionId,
                bookId = bookId,
            )

            coEvery {
                listsRemoteDataSource.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    editionId = editionId,
                )
            } throws CancellationException("cancelled")

            // ----- Act & Assert -----
            shouldThrow<CancellationException> {
                repository.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    edition = edition,
                )
            }

            coVerify(exactly = 0) {
                listsLocalDataSource.removeOptimisticListBook(
                    listId = any(),
                    bookId = any(),
                )
            }

            coVerify(exactly = 0) {
                listWriteQueue.enqueue(payload = any())
            }
        }
    }

    @Nested
    inner class RemoveBookFromList {
        @Test
        fun `returns early without calling remote when book is not in the list`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3

            coEvery {
                listsLocalDataSource.findListBookByListAndBook(
                    listId = listId,
                    bookId = bookId,
                )
            } returns null

            // ----- Act -----
            repository.removeBookFromList(
                listId = listId,
                bookId = bookId,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                listsRemoteDataSource.removeListBook(book = any())
            }
        }

        @Test
        fun `removes locally by listBookId, then caches the BookList returned by remote`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val snapshot = stubListBook(
                listBookId = 42,
                listId = listId,
                bookId = bookId,
            )
            val updatedBookList = stubBookList()

            coEvery {
                listsLocalDataSource.findListBookByListAndBook(
                    listId = listId,
                    bookId = bookId,
                )
            } returns snapshot

            coEvery {
                listsRemoteDataSource.removeListBook(book = snapshot)
            } returns updatedBookList

            // ----- Act -----
            repository.removeBookFromList(
                listId = listId,
                bookId = bookId,
            )

            // ----- Assert -----
            coVerify {
                listsLocalDataSource.removeListBookById(listBookId = snapshot.listBookId)
            }

            coVerify {
                listsLocalDataSource.cacheUserBookLists(lists = listOf(updatedBookList))
            }
        }

        @Test
        fun `on a retryable failure returns normally, does not restore the entry and enqueues REMOVE_LIST_BOOK`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val editionId = 10
            val listBookId = 42
            val snapshot = stubListBook(
                listBookId = listBookId,
                listId = listId,
                bookId = bookId,
                editionId = editionId,
            )

            coEvery {
                listsLocalDataSource.findListBookByListAndBook(
                    listId = listId,
                    bookId = bookId,
                )
            } returns snapshot

            coEvery {
                listsRemoteDataSource.removeListBook(book = snapshot)
            } throws OfflineException()

            val slot = mutableListOf<PendingListWrite>()

            coJustRun {
                listWriteQueue.enqueue(capture(slot))
            }

            // ----- Act -----
            repository.removeBookFromList(
                listId = listId,
                bookId = bookId,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                listWriteQueue.enqueue(payload = any())
            }

            val enqueued = slot.first()

            enqueued.kind shouldBe PendingListWriteKind.REMOVE_LIST_BOOK
            enqueued.listId shouldBe listId
            enqueued.bookId shouldBe bookId
            enqueued.editionId shouldBe editionId
            enqueued.listBookId shouldBe listBookId

            coVerify(exactly = 0) {
                listsLocalDataSource.cacheListBook(book = any())
            }
        }

        @Test
        fun `on a rejected failure restores the snapshot, rethrows and does not enqueue`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val snapshot = stubListBook(
                listBookId = 42,
                listId = listId,
                bookId = bookId,
            )
            val remoteError = RuntimeException("rejected")

            coEvery {
                listsLocalDataSource.findListBookByListAndBook(
                    listId = listId,
                    bookId = bookId,
                )
            } returns snapshot

            coEvery {
                listsRemoteDataSource.removeListBook(book = snapshot)
            } throws remoteError

            // ----- Act -----
            val thrown = shouldThrow<RuntimeException> {
                repository.removeBookFromList(
                    listId = listId,
                    bookId = bookId,
                )
            }

            // ----- Assert -----
            thrown shouldBe remoteError

            coVerify(exactly = 1) {
                listsLocalDataSource.cacheListBook(book = snapshot)
            }

            coVerify(exactly = 0) {
                listWriteQueue.enqueue(payload = any())
            }
        }

        @Test
        fun `when enqueueing a retryable failure also fails, restores the snapshot and rethrows the queue error`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val snapshot = stubListBook(
                listBookId = 42,
                listId = listId,
                bookId = bookId,
            )
            val queueError = IllegalStateException("queue full")

            coEvery {
                listsLocalDataSource.findListBookByListAndBook(
                    listId = listId,
                    bookId = bookId,
                )
            } returns snapshot

            coEvery {
                listsRemoteDataSource.removeListBook(book = snapshot)
            } throws OfflineException()

            coEvery {
                listWriteQueue.enqueue(payload = any())
            } throws queueError

            // ----- Act -----
            val thrown = shouldThrow<IllegalStateException> {
                repository.removeBookFromList(
                    listId = listId,
                    bookId = bookId,
                )
            }

            // ----- Assert -----
            thrown shouldBe queueError

            coVerify(exactly = 1) {
                listsLocalDataSource.cacheListBook(book = snapshot)
            }
        }

        @Test
        fun `cancellation — rethrows CancellationException without restoring snapshot`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val snapshot = stubListBook(
                listBookId = 42,
                listId = listId,
                bookId = bookId,
            )

            coEvery {
                listsLocalDataSource.findListBookByListAndBook(
                    listId = listId,
                    bookId = bookId,
                )
            } returns snapshot

            coEvery {
                listsRemoteDataSource.removeListBook(book = snapshot)
            } throws CancellationException("cancelled")

            // ----- Act & Assert -----
            shouldThrow<CancellationException> {
                repository.removeBookFromList(
                    listId = listId,
                    bookId = bookId,
                )
            }

            coVerify(exactly = 0) {
                listsLocalDataSource.cacheListBook(book = any())
            }

            coVerify(exactly = 0) {
                listWriteQueue.enqueue(payload = any())
            }
        }
    }
}
