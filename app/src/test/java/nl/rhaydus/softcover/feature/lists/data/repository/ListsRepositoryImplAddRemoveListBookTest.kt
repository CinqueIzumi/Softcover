package nl.rhaydus.softcover.feature.lists.data.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListsRepositoryImplAddRemoveListBookTest {

    private lateinit var listsRemoteDataSource: ListsRemoteDataSource
    private lateinit var listsLocalDataSource: ListsLocalDataSource
    private lateinit var repository: ListsRepositoryImpl

    @BeforeEach
    fun setUp() {
        listsRemoteDataSource = mockk()
        listsLocalDataSource = mockk(relaxed = true)

        repository = ListsRepositoryImpl(
            listsRemoteDataSource = listsRemoteDataSource,
            listsLocalDataSource = listsLocalDataSource,
            applicationScope = ApplicationScope(scope = CoroutineScope(UnconfinedTestDispatcher())),
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
        name = "my list",
        slug = "my-list",
        books = emptyList(),
    )

    // ----- AddBookToList -----

    @Nested
    inner class AddBookToList {

        @Test
        fun `optimistically caches placeholder with OPTIMISTIC_LIST_BOOK_ID before calling remote`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val editionId = 10
            val edition = stubEdition(id = editionId, bookId = bookId)
            val realListBook = stubListBook(listBookId = 99, listId = listId, bookId = bookId, editionId = editionId)

            coEvery {
                listsLocalDataSource.findListBookByListAndBook(
                    listId = listId,
                    bookId = bookId,
                )
            } returns null

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
        }

        @Test
        fun `on remote success removes optimistic entry and caches the real ListBook`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val editionId = 10
            val edition = stubEdition(id = editionId, bookId = bookId)
            val realListBook = stubListBook(listBookId = 99, listId = listId, bookId = bookId, editionId = editionId)

            coEvery {
                listsLocalDataSource.findListBookByListAndBook(
                    listId = listId,
                    bookId = bookId,
                )
            } returns null

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
        fun `on remote failure clears optimistic entry and restores prior snapshot`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val editionId = 10
            val edition = stubEdition(id = editionId, bookId = bookId)
            val priorSnapshot = stubListBook(listBookId = 7, listId = listId, bookId = bookId, editionId = editionId)
            val remoteError = RuntimeException("network error")

            coEvery {
                listsLocalDataSource.findListBookByListAndBook(
                    listId = listId,
                    bookId = bookId,
                )
            } returns priorSnapshot

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

            coVerify {
                listsLocalDataSource.removeOptimisticListBook(
                    listId = listId,
                    bookId = bookId,
                )
            }

            coVerify {
                listsLocalDataSource.cacheListBook(book = priorSnapshot)
            }
        }

        @Test
        fun `on remote failure with null snapshot clears optimistic entry but does not re-cache`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val editionId = 10
            val edition = stubEdition(id = editionId, bookId = bookId)
            val remoteError = RuntimeException("network error")

            coEvery {
                listsLocalDataSource.findListBookByListAndBook(
                    listId = listId,
                    bookId = bookId,
                )
            } returns null

            coEvery {
                listsRemoteDataSource.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    editionId = editionId,
                )
            } throws remoteError

            // ----- Act -----
            runCatching {
                repository.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    edition = edition,
                )
            }

            // ----- Assert -----
            coVerify {
                listsLocalDataSource.removeOptimisticListBook(
                    listId = listId,
                    bookId = bookId,
                )
            }

            // Only the optimistic insert should have been cached — no restore call
            coVerify(exactly = 1) {
                listsLocalDataSource.cacheListBook(book = any())
            }
        }

        @Test
        fun `cancellation — rethrows CancellationException without rollback`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val editionId = 10
            val edition = stubEdition(id = editionId, bookId = bookId)

            coEvery {
                listsLocalDataSource.findListBookByListAndBook(
                    listId = listId,
                    bookId = bookId,
                )
            } returns null

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
        }
    }

    // ----- RemoveBookFromList -----

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
            val snapshot = stubListBook(listBookId = 42, listId = listId, bookId = bookId)
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
            val snapshot = stubListBook(listBookId = 42, listId = listId, bookId = bookId)
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
        fun `on remote failure restores snapshot and rethrows`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val snapshot = stubListBook(listBookId = 42, listId = listId, bookId = bookId)
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

            coVerify {
                listsLocalDataSource.cacheListBook(book = snapshot)
            }
        }

        @Test
        fun `cancellation — rethrows CancellationException without restoring snapshot`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val bookId = 3
            val snapshot = stubListBook(listBookId = 42, listId = listId, bookId = bookId)

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
        }
    }
}
