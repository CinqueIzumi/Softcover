package nl.rhaydus.softcover.feature.lists.data.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteQueue
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListsRepositoryImplOwnedEditionTest {

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

    private fun stubEdition(id: Int, bookId: Int = 1): BookEdition = mockk {
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
        name = "owned",
        slug = "owned",
        books = emptyList(),
    )

    @Nested
    inner class MarkEditionAsOwned {

        @Test
        fun `optimistically caches placeholder with OPTIMISTIC_LIST_BOOK_ID when getOwnedListId returns non-null`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val bookId = 5
            val ownedListId = 99
            val edition = stubEdition(id = editionId, bookId = bookId)
            val realListBook = stubListBook(listBookId = 42, listId = ownedListId, bookId = bookId, editionId = editionId)

            coEvery {
                listsLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns null

            coEvery {
                listsLocalDataSource.getOwnedListId()
            } returns ownedListId

            coEvery {
                listsRemoteDataSource.markEditionAsOwned(edition = edition)
            } returns realListBook

            // ----- Act -----
            repository.markEditionAsOwned(edition = edition)

            // ----- Assert -----
            coVerify {
                listsLocalDataSource.cacheListBook(
                    book = ListBook(
                        listBookId = 0,
                        listId = ownedListId,
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
        fun `skips optimistic insert when getOwnedListId returns null but still calls remote`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val bookId = 5
            val edition = stubEdition(id = editionId, bookId = bookId)
            val realListBook = stubListBook(listBookId = 42, bookId = bookId, editionId = editionId)

            coEvery {
                listsLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns null

            coEvery {
                listsLocalDataSource.getOwnedListId()
            } returns null

            coEvery {
                listsRemoteDataSource.markEditionAsOwned(edition = edition)
            } returns realListBook

            // ----- Act -----
            repository.markEditionAsOwned(edition = edition)

            // ----- Assert -----
            // Only the real list book should be cached (no optimistic placeholder)
            coVerify(exactly = 1) {
                listsLocalDataSource.cacheListBook(book = any())
            }

            coVerify {
                listsRemoteDataSource.markEditionAsOwned(edition = edition)
            }

            coVerify {
                listsLocalDataSource.cacheListBook(book = realListBook)
            }
        }

        @Test
        fun `on success removes optimistic entry and caches the real ListBook`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val bookId = 5
            val ownedListId = 99
            val edition = stubEdition(id = editionId, bookId = bookId)
            val realListBook = stubListBook(listBookId = 42, listId = ownedListId, bookId = bookId, editionId = editionId)

            coEvery {
                listsLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns null

            coEvery {
                listsLocalDataSource.getOwnedListId()
            } returns ownedListId

            coEvery {
                listsRemoteDataSource.markEditionAsOwned(edition = edition)
            } returns realListBook

            // ----- Act -----
            repository.markEditionAsOwned(edition = edition)

            // ----- Assert -----
            coVerify {
                listsLocalDataSource.removeOwnedListBookByEditionId(editionId = editionId)
            }

            coVerify {
                listsLocalDataSource.cacheListBook(book = realListBook)
            }
        }

        @Test
        fun `on failure restores snapshot via findOwnedListBookByEditionId and rethrows`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val bookId = 5
            val edition = stubEdition(id = editionId, bookId = bookId)
            val snapshot = stubListBook(listBookId = 7, bookId = bookId, editionId = editionId)
            val remoteError = RuntimeException("network error")

            coEvery {
                listsLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns snapshot

            coEvery {
                listsLocalDataSource.getOwnedListId()
            } returns null

            coEvery {
                listsRemoteDataSource.markEditionAsOwned(edition = edition)
            } throws remoteError

            // ----- Act -----
            val caught = runCatching { repository.markEditionAsOwned(edition = edition) }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError

            coVerify {
                listsLocalDataSource.removeOwnedListBookByEditionId(editionId = editionId)
            }

            coVerify {
                listsLocalDataSource.cacheListBook(book = snapshot)
            }
        }

        @Test
        fun `on failure with null snapshot does not re-cache anything`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val bookId = 5
            val edition = stubEdition(id = editionId, bookId = bookId)
            val remoteError = RuntimeException("network error")

            coEvery {
                listsLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns null

            coEvery {
                listsLocalDataSource.getOwnedListId()
            } returns null

            coEvery {
                listsRemoteDataSource.markEditionAsOwned(edition = edition)
            } throws remoteError

            // ----- Act -----
            runCatching { repository.markEditionAsOwned(edition = edition) }

            // ----- Assert -----
            coVerify(exactly = 0) {
                listsLocalDataSource.cacheListBook(book = any())
            }
        }

        @Test
        fun `cancellation — rethrows CancellationException without rollback`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val bookId = 5
            val edition = stubEdition(id = editionId, bookId = bookId)

            coEvery {
                listsLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns null

            coEvery {
                listsLocalDataSource.getOwnedListId()
            } returns null

            coEvery {
                listsRemoteDataSource.markEditionAsOwned(edition = edition)
            } throws CancellationException("cancelled")

            // ----- Act & Assert -----
            shouldThrow<CancellationException> {
                repository.markEditionAsOwned(edition = edition)
            }

            coVerify(exactly = 0) {
                listsLocalDataSource.removeOwnedListBookByEditionId(editionId = any())
            }
        }
    }

    @Nested
    inner class RemoveOwnedEdition {

        @Test
        fun `happy path — removes locally, calls remote, caches updated BookList`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val snapshot = stubListBook(listBookId = 5, editionId = editionId)
            val updatedBookList = stubBookList()

            coEvery {
                listsLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns snapshot

            coEvery {
                listsRemoteDataSource.removeListBook(book = snapshot)
            } returns updatedBookList

            // ----- Act -----
            repository.removeOwnedEdition(editionId = editionId)

            // ----- Assert -----
            coVerify {
                listsLocalDataSource.removeOwnedListBookByEditionId(editionId = editionId)
            }

            coVerify {
                listsRemoteDataSource.removeListBook(book = snapshot)
            }

            coVerify {
                listsLocalDataSource.cacheUserBookLists(lists = listOf(updatedBookList))
            }
        }

        @Test
        fun `when snapshot is null returns early without calling remote`() = runTest {
            // ----- Arrange -----
            val editionId = 10

            coEvery {
                listsLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns null

            // ----- Act -----
            repository.removeOwnedEdition(editionId = editionId)

            // ----- Assert -----
            coVerify(exactly = 0) {
                listsRemoteDataSource.removeListBook(book = any())
            }
        }

        @Test
        fun `on failure enqueues REMOVE_LIST_BOOK with snapshot fields and rethrows`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val snapshot = stubListBook(listBookId = 5, editionId = editionId)
            val remoteError = RuntimeException("network error")

            coEvery {
                listsLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns snapshot

            coEvery {
                listsRemoteDataSource.removeListBook(book = snapshot)
            } throws remoteError

            // ----- Act -----
            val caught = runCatching { repository.removeOwnedEdition(editionId = editionId) }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError

            coVerify(exactly = 1) {
                listWriteQueue.enqueue(write = any())
            }

            coVerify(exactly = 0) {
                listsLocalDataSource.cacheListBook(book = any())
            }
        }

        @Test
        fun `cancellation — rethrows CancellationException without re-caching snapshot`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val snapshot = stubListBook(listBookId = 5, editionId = editionId)

            coEvery {
                listsLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns snapshot

            coEvery {
                listsRemoteDataSource.removeListBook(book = snapshot)
            } throws CancellationException("cancelled")

            // ----- Act & Assert -----
            shouldThrow<CancellationException> {
                repository.removeOwnedEdition(editionId = editionId)
            }

            coVerify(exactly = 0) {
                listsLocalDataSource.cacheListBook(book = any())
            }
        }
    }
}
