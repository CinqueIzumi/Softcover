package nl.rhaydus.softcover.feature.books.data.repository

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.books.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.feature.books.data.datasource.BooksRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BooksRepositoryImplTest {

    private lateinit var booksRemoteDataSource: BooksRemoteDataSource
    private lateinit var booksLocalDataSource: BooksLocalDataSource
    private lateinit var repository: BooksRepositoryImpl

    @BeforeEach
    fun setUp() {
        booksRemoteDataSource = mockk()
        booksLocalDataSource = mockk(relaxed = true)
        repository = BooksRepositoryImpl(
            booksRemoteDataSource = booksRemoteDataSource,
            booksLocalDataSource = booksLocalDataSource,
        )
    }

    private fun stubBook(userBookId: Int?): Book = mockk {
        every {
            this@mockk.userBook
        } returns if (userBookId != null) stubUserBook(userBookId) else null
    }

    private fun stubUserBook(id: Int): UserBook = mockk {
        every {
            this@mockk.id
        } returns id
    }

    private fun stubBookList(): BookList = mockk()

    private fun stubListBook(): ListBook = mockk()

    private fun stubBookEdition(): BookEdition = mockk()

    @Nested
    inner class Books {

        @Test
        fun `books property is wired to local data source allUserBooks flow`() = runTest {
            // ----- Arrange -----
            val expectedBooks = listOf(stubBook(userBookId = 1))

            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(expectedBooks)

            val freshRepository = BooksRepositoryImpl(
                booksRemoteDataSource = booksRemoteDataSource,
                booksLocalDataSource = booksLocalDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.books.test {
                awaitItem() shouldBe expectedBooks
                awaitComplete()
            }
        }
    }

    @Nested
    inner class AllUserLists {

        @Test
        fun `allUserLists property is wired to local data source allUserLists flow`() = runTest {
            // ----- Arrange -----
            val expectedLists = listOf(stubBookList())

            every {
                booksLocalDataSource.allUserLists
            } returns flowOf(expectedLists)

            val freshRepository = BooksRepositoryImpl(
                booksRemoteDataSource = booksRemoteDataSource,
                booksLocalDataSource = booksLocalDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.allUserLists.test {
                awaitItem() shouldBe expectedLists
                awaitComplete()
            }
        }
    }

    @Nested
    inner class GetBooksFlowByStatus {

        @Test
        fun `getBooksFlowByStatus delegates to local data source with the given status`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.CURRENTLY_READING
            val expectedBooks = listOf(stubBook(userBookId = 5))

            every {
                booksLocalDataSource.getBooksFlowByStatus(status = status)
            } returns flowOf(expectedBooks)

            // ----- Act & Assert -----
            repository.getBooksFlowByStatus(status = status).test {
                awaitItem() shouldBe expectedBooks
                awaitComplete()
            }
        }
    }

    @Nested
    inner class InitializeBooks {

        @Test
        fun `initializeBooks fetches remote books and lists then caches them`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val fetchedBooks = listOf(
                stubBook(userBookId = 1),
                stubBook(userBookId = 2),
            )
            val fetchedLists = listOf(stubBookList())

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId)
            } returns fetchedBooks

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId)
            } returns fetchedLists

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBooks(books = fetchedBooks)
            }
            coVerify {
                booksLocalDataSource.cacheUserBookLists(lists = fetchedLists)
            }
        }

        @Test
        fun `initializeBooks removes locally stored books absent from remote fetch`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val bookStillPresent = stubBook(userBookId = 1)
            val fetchedBooks = listOf(bookStillPresent)
            val fetchedLists = emptyList<BookList>()
            val staleLocalId = 99

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId)
            } returns fetchedBooks

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId)
            } returns fetchedLists

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns listOf(
                1,
                staleLocalId,
            )

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.removeUserBooksById(ids = listOf(staleLocalId))
            }
        }

        @Test
        fun `initializeBooks does not remove books whose ids are present in the remote fetch`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val fetchedBooks = listOf(stubBook(userBookId = 7))
            val fetchedLists = emptyList<BookList>()

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId)
            } returns fetchedBooks

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId)
            } returns fetchedLists

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns listOf(7)

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.removeUserBooksById(ids = emptyList())
            }
        }

        @Test
        fun `initializeBooks handles books without userBook when computing ids to remove`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val bookWithoutUserBook = stubBook(userBookId = null)
            val fetchedBooks = listOf(bookWithoutUserBook)
            val fetchedLists = emptyList<BookList>()

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId)
            } returns fetchedBooks

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId)
            } returns fetchedLists

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns listOf(3)

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.removeUserBooksById(ids = listOf(3))
            }
        }

        @Test
        fun `initializeBooks throws when called a second time in the same session`() = runTest {
            // ----- Arrange -----
            val userId = 10

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId)
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            repository.initializeBooks(userId = userId)

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                repository.initializeBooks(userId = userId)
            }
        }

        @Test
        fun `initializeBooks does not throw on first call`() = runTest {
            // ----- Arrange -----
            val userId = 1

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId)
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            val result = runCatching { repository.initializeBooks(userId = userId) }

            // ----- Assert -----
            result.isSuccess shouldBe true
        }
    }

    @Nested
    inner class RefreshUserBooks {

        @Test
        fun `refreshUserBooks fetches remote books and lists then caches them`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val fetchedBooks = listOf(stubBook(userBookId = 3))
            val fetchedLists = listOf(stubBookList())

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId)
            } returns fetchedBooks

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId)
            } returns fetchedLists

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBooks(books = fetchedBooks)
            }
            coVerify {
                booksLocalDataSource.cacheUserBookLists(lists = fetchedLists)
            }
        }

        @Test
        fun `refreshUserBooks does not set initializedBooksThisSession so initializeBooks still throws after a prior init`() = runTest {
            // ----- Arrange -----
            val userId = 20

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId)
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            repository.initializeBooks(userId = userId)
            repository.refreshUserBooks(userId = userId)

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                repository.initializeBooks(userId = userId)
            }
        }

        @Test
        fun `refreshUserBooks can be called multiple times without throwing`() = runTest {
            // ----- Arrange -----
            val userId = 20

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId)
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            val result = runCatching {
                repository.refreshUserBooks(userId = userId)
                repository.refreshUserBooks(userId = userId)
            }

            // ----- Assert -----
            result.isSuccess shouldBe true
        }
    }

    @Nested
    inner class CacheBook {

        @Test
        fun `cacheBook delegates to local data source`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)

            // ----- Act -----
            repository.cacheBook(book = book)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBook(book = book)
            }
        }
    }

    @Nested
    inner class RemoveBook {

        @Test
        fun `removeBook removes the user book id via local data source`() = runTest {
            // ----- Arrange -----
            val userBookId = 42
            val book = stubBook(userBookId = userBookId)

            // ----- Act -----
            repository.removeBook(book = book)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.removeUserBooksById(ids = listOf(userBookId))
            }
        }

        @Test
        fun `removeBook throws when book has no userBook`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = null)

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                repository.removeBook(book = book)
            }
        }
    }

    @Nested
    inner class RemoveAllBooks {

        @Test
        fun `removeAllBooks delegates to local data source`() = runTest {
            // ----- Arrange -----
            // (no additional setup needed — booksLocalDataSource is relaxed)

            // ----- Act -----
            repository.removeAllBooks()

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.removeAllBooks()
            }
        }

        @Test
        fun `removeAllBooks resets session flag so initializeBooks can be called again`() = runTest {
            // ----- Arrange -----
            val userId = 5

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId)
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            repository.initializeBooks(userId = userId)
            repository.removeAllBooks()

            // ----- Act -----
            val result = runCatching { repository.initializeBooks(userId = userId) }

            // ----- Assert -----
            result.isSuccess shouldBe true
        }
    }

    @Nested
    inner class FetchBookById {

        @Test
        fun `fetchBookById delegates to remote data source and returns result`() = runTest {
            // ----- Arrange -----
            val bookId = 7
            val expectedBook = stubBook(userBookId = null)

            coEvery {
                booksRemoteDataSource.fetchBookById(id = bookId)
            } returns expectedBook

            // ----- Act -----
            val result = repository.fetchBookById(id = bookId)

            // ----- Assert -----
            result shouldBe expectedBook
        }
    }

    @Nested
    inner class MarkBookAsWantToRead {

        @Test
        fun `markBookAsWantToRead delegates to remote data source and returns result`() = runTest {
            // ----- Arrange -----
            val bookId = 11
            val expectedBook = stubBook(userBookId = null)

            coEvery {
                booksRemoteDataSource.markBookAsWantToRead(bookId = bookId)
            } returns expectedBook

            // ----- Act -----
            val result = repository.markBookAsWantToRead(bookId = bookId)

            // ----- Assert -----
            result shouldBe expectedBook
        }
    }

    @Nested
    inner class MarkBookAsReading {

        @Test
        fun `markBookAsReading delegates to remote data source and returns result`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)
            val expectedBook = stubBook(userBookId = 1)

            coEvery {
                booksRemoteDataSource.markBookAsReading(book)
            } returns expectedBook

            // ----- Act -----
            val result = repository.markBookAsReading(book = book)

            // ----- Assert -----
            result shouldBe expectedBook
        }
    }

    @Nested
    inner class RemoveBookFromLibrary {

        @Test
        fun `removeBookFromLibrary delegates to remote data source`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            } returns Unit

            // ----- Act -----
            repository.removeBookFromLibrary(book = book)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            }
        }
    }

    @Nested
    inner class UpdateBookProgress {

        @Test
        fun `updateBookProgress delegates to remote data source with correct arguments and returns result`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)
            val newPage = 150
            val expectedBook = stubBook(userBookId = 1)

            coEvery {
                booksRemoteDataSource.updateBookProgress(book = book, newPage = newPage)
            } returns expectedBook

            // ----- Act -----
            val result = repository.updateBookProgress(book = book, newPage = newPage)

            // ----- Assert -----
            result shouldBe expectedBook
        }
    }

    @Nested
    inner class MarkBookAsRead {

        @Test
        fun `markBookAsRead delegates to remote data source and returns result`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)
            val expectedBook = stubBook(userBookId = 1)

            coEvery {
                booksRemoteDataSource.markBookAsRead(book = book)
            } returns expectedBook

            // ----- Act -----
            val result = repository.markBookAsRead(book = book)

            // ----- Assert -----
            result shouldBe expectedBook
        }
    }

    @Nested
    inner class UpdateBookEdition {

        @Test
        fun `updateBookEdition delegates to remote data source with correct arguments and returns result`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 3)
            val newEditionId = 88
            val expectedBook = stubBook(userBookId = 3)

            coEvery {
                booksRemoteDataSource.updateBookEdition(userBook = userBook, newEditionId = newEditionId)
            } returns expectedBook

            // ----- Act -----
            val result = repository.updateBookEdition(userBook = userBook, newEditionId = newEditionId)

            // ----- Assert -----
            result shouldBe expectedBook
        }
    }

    @Nested
    inner class MarkEditionAsOwned {

        @Test
        fun `markEditionAsOwned delegates to remote data source and returns result`() = runTest {
            // ----- Arrange -----
            val edition = stubBookEdition()
            val expectedListBook = stubListBook()

            coEvery {
                booksRemoteDataSource.markEditionAsOwned(edition = edition)
            } returns expectedListBook

            // ----- Act -----
            val result = repository.markEditionAsOwned(edition = edition)

            // ----- Assert -----
            result shouldBe expectedListBook
        }
    }

    @Nested
    inner class GetListBookByEditionId {

        @Test
        fun `getListBookByEditionId delegates to local data source and returns result`() = runTest {
            // ----- Arrange -----
            val editionId = 55
            val expectedListBook = stubListBook()

            coEvery {
                booksLocalDataSource.getOwnedListBookByEditionId(editionId = editionId)
            } returns expectedListBook

            // ----- Act -----
            val result = repository.getListBookByEditionId(editionId = editionId)

            // ----- Assert -----
            result shouldBe expectedListBook
        }
    }

    @Nested
    inner class RemoveListBook {

        @Test
        fun `removeListBook fetches updated list from remote and caches it locally`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook()
            val updatedBookList = stubBookList()

            coEvery {
                booksRemoteDataSource.removeListBook(book = listBook)
            } returns updatedBookList

            // ----- Act -----
            repository.removeListBook(book = listBook)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheUserBookLists(lists = listOf(updatedBookList))
            }
        }

        @Test
        fun `removeListBook calls remote before caching locally`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook()
            val updatedBookList = stubBookList()

            coEvery {
                booksRemoteDataSource.removeListBook(book = listBook)
            } returns updatedBookList

            // ----- Act -----
            repository.removeListBook(book = listBook)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.removeListBook(book = listBook)
            }
            coVerify {
                booksLocalDataSource.cacheUserBookLists(lists = listOf(updatedBookList))
            }
        }
    }

    @Nested
    inner class CacheListBook {

        @Test
        fun `cacheListBook delegates to local data source`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook()

            // ----- Act -----
            repository.cacheListBook(book = listBook)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheListBook(book = listBook)
            }
        }
    }
}
