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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.coroutines.cancellation.CancellationException
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteQueue
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWriteKind
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.core.lists.data.datasource.ListsRemoteDataSource

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
        fun `optimistically caches placeholder with OPTIMISTIC_LIST_BOOK_ID before calling remote`() = runTest {
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
        }

        @Test
        fun `on remote success removes optimistic entry and caches the real ListBook`() = runTest {
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
        fun `on remote failure optimistic entry stays — no removeOptimisticListBook call`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val editionId = 10
            val edition = stubEdition(
                id = editionId,
                bookId = bookId,
            )
            val remoteError = RuntimeException("network error")

            coEvery {
                listsRemoteDataSource.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    editionId = editionId,
                )
            } throws remoteError

            // ----- Act -----
            val caught = runCatching {
                repository.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    edition = edition,
                )
            }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError

            coVerify(exactly = 0) {
                listsLocalDataSource.removeOptimisticListBook(
                    listId = any(),
                    bookId = any(),
                )
            }
        }

        @Test
        fun `on remote failure enqueues ADD_LIST_BOOK with listId bookId editionId and rethrows`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val editionId = 10
            val edition = stubEdition(
                id = editionId,
                bookId = bookId,
            )
            val remoteError = RuntimeException("network error")

            coEvery {
                listsRemoteDataSource.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    editionId = editionId,
                )
            } throws remoteError

            val slot = mutableListOf<PendingListWrite>()

            coJustRun {
                listWriteQueue.enqueue(capture(slot))
            }

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
                listWriteQueue.enqueue(payload = any())
            }

            val enqueued = slot.first()

            enqueued.kind shouldBe PendingListWriteKind.ADD_LIST_BOOK
            enqueued.listId shouldBe listId
            enqueued.bookId shouldBe bookId
            enqueued.editionId shouldBe editionId
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
        fun `optimistically removes by listBookId before calling remote`() = runTest {
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
        }

        @Test
        fun `on remote success caches the returned BookList`() = runTest {
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
                listsLocalDataSource.cacheUserBookLists(lists = listOf(updatedBookList))
            }
        }

        @Test
        fun `on remote failure locally removed entry is NOT restored`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val snapshot = stubListBook(
                listBookId = 42,
                listId = listId,
                bookId = bookId,
            )
            val remoteError = RuntimeException("network error")

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
            val caught = runCatching {
                repository.removeBookFromList(
                    listId = listId,
                    bookId = bookId,
                )
            }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError

            coVerify(exactly = 0) {
                listsLocalDataSource.cacheListBook(book = any())
            }
        }

        @Test
        fun `on remote failure enqueues REMOVE_LIST_BOOK with snapshot fields and rethrows`() = runTest {
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
            val remoteError = RuntimeException("network error")

            coEvery {
                listsLocalDataSource.findListBookByListAndBook(
                    listId = listId,
                    bookId = bookId,
                )
            } returns snapshot

            coEvery {
                listsRemoteDataSource.removeListBook(book = snapshot)
            } throws remoteError

            val slot = mutableListOf<PendingListWrite>()

            coJustRun {
                listWriteQueue.enqueue(capture(slot))
            }

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
                listWriteQueue.enqueue(payload = any())
            }

            val enqueued = slot.first()

            enqueued.kind shouldBe PendingListWriteKind.REMOVE_LIST_BOOK
            enqueued.listId shouldBe listId
            enqueued.bookId shouldBe bookId
            enqueued.editionId shouldBe editionId
            enqueued.listBookId shouldBe listBookId
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
