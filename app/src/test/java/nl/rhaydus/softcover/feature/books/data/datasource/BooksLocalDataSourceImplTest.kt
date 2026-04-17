package nl.rhaydus.softcover.feature.books.data.datasource

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.books.data.dao.BookDao
import nl.rhaydus.softcover.feature.books.data.mapper.toModel
import nl.rhaydus.softcover.feature.books.data.model.AuthorEntity
import nl.rhaydus.softcover.feature.books.data.model.BookEditionEntity
import nl.rhaydus.softcover.feature.books.data.model.BookEditionView
import nl.rhaydus.softcover.feature.books.data.model.BookEditionWithAuthors
import nl.rhaydus.softcover.feature.books.data.model.BookEntity
import nl.rhaydus.softcover.feature.books.data.model.BookFullEntity
import nl.rhaydus.softcover.feature.books.data.model.BookListEntity
import nl.rhaydus.softcover.feature.books.data.model.BookListWithBooks
import nl.rhaydus.softcover.feature.books.data.model.ListBookEntity
import nl.rhaydus.softcover.feature.books.data.model.ListBookFull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BooksLocalDataSourceImplTest {

    private lateinit var dao: BookDao
    private lateinit var dataSource: BooksLocalDataSourceImpl

    @BeforeEach
    fun setUp() {
        dao = mockk(relaxed = true)
        dataSource = BooksLocalDataSourceImpl(dao = dao)
    }

    private fun stubBookEntity(id: Int = 1): BookEntity = BookEntity(
        id = id,
        title = "Test Book $id",
        defaultEditionId = null,
        rating = 4.0,
        description = "Description",
        releaseYear = 2020,
        coverUrl = "https://example.com/cover.jpg",
        usersCount = 100,
        positionInSeries = null,
        seriesId = null,
    )

    private fun stubEditionEntity(id: Int = 10, bookId: Int = 1): BookEditionEntity = BookEditionEntity(
        id = id,
        bookId = bookId,
        publisher = null,
        title = null,
        url = null,
        isbn10 = null,
        pages = null,
        releaseYear = 2020,
        format = "Paperback",
    )

    private fun stubEditionWithAuthors(
        id: Int = 10,
        bookId: Int = 1,
    ): BookEditionWithAuthors = BookEditionWithAuthors(
        edition = BookEditionView(
            edition = stubEditionEntity(
                id = id,
                bookId = bookId,
            ),
            isOwned = false,
        ),
        authors = emptyList(),
    )

    private fun stubBookFullEntity(id: Int = 1): BookFullEntity = BookFullEntity(
        book = stubBookEntity(id = id),
        bookAuthors = emptyList(),
        series = null,
        editions = listOf(stubEditionWithAuthors(bookId = id)),
        userBookWithJournals = null,
    )

    private fun stubBookListWithBooks(id: Int = 1): BookListWithBooks = BookListWithBooks(
        bookList = BookListEntity(
            id = id,
            name = "My List $id",
            slug = "my-list-$id",
        ),
        listBooks = emptyList(),
    )

    private fun stubListBookFull(
        listBookId: Int = 1,
        listId: Int = 1,
        bookId: Int = 1,
        editionId: Int = 10,
    ): ListBookFull = ListBookFull(
        listBook = ListBookEntity(
            listBookId = listBookId,
            listId = listId,
            bookId = bookId,
            editionId = editionId,
        ),
        book = stubBookFullEntity(id = bookId),
        edition = stubEditionWithAuthors(
            id = editionId,
            bookId = bookId,
        ),
    )

    @Nested
    inner class AllUserBooks {

        @Test
        fun `emits mapped domain books from DAO observeBooks`() = runTest {
            // ----- Arrange -----
            val entity = stubBookFullEntity(id = 1)

            every {
                dao.observeBooks()
            } returns flowOf(listOf(entity))

            // ----- Act & Assert -----
            dataSource.allUserBooks.test {
                awaitItem() shouldBe listOf(entity.toModel())
                awaitComplete()
            }
        }

        @Test
        fun `emits empty list when DAO emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                dao.observeBooks()
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            dataSource.allUserBooks.test {
                awaitItem() shouldBe emptyList<Book>()
                awaitComplete()
            }
        }

        @Test
        fun `emits multiple mapped books when DAO emits multiple entities`() = runTest {
            // ----- Arrange -----
            val entity1 = stubBookFullEntity(id = 1)
            val entity2 = stubBookFullEntity(id = 2)

            every {
                dao.observeBooks()
            } returns flowOf(listOf(entity1, entity2))

            // ----- Act & Assert -----
            dataSource.allUserBooks.test {
                awaitItem() shouldBe listOf(entity1.toModel(), entity2.toModel())
                awaitComplete()
            }
        }

        @Test
        fun `suppresses consecutive duplicate emissions via distinctUntilChanged`() = runTest {
            // ----- Arrange -----
            val entity = stubBookFullEntity(id = 1)
            val list = listOf(entity)

            every {
                dao.observeBooks()
            } returns flowOf(list, list)

            // ----- Act & Assert -----
            dataSource.allUserBooks.test {
                awaitItem() shouldBe listOf(entity.toModel())
                awaitComplete()
            }
        }
    }

    @Nested
    inner class AllUserLists {

        @Test
        fun `emits mapped domain book lists from DAO observeBookLists`() = runTest {
            // ----- Arrange -----
            val entity = stubBookListWithBooks(id = 1)

            every {
                dao.observeBookLists()
            } returns flowOf(listOf(entity))

            // ----- Act & Assert -----
            dataSource.allUserLists.test {
                awaitItem() shouldBe listOf(entity.toModel())
                awaitComplete()
            }
        }

        @Test
        fun `emits empty list when DAO emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                dao.observeBookLists()
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            dataSource.allUserLists.test {
                awaitItem() shouldBe emptyList<BookList>()
                awaitComplete()
            }
        }

        @Test
        fun `suppresses consecutive duplicate emissions via distinctUntilChanged`() = runTest {
            // ----- Arrange -----
            val entity = stubBookListWithBooks(id = 1)
            val list = listOf(entity)

            every {
                dao.observeBookLists()
            } returns flowOf(list, list)

            // ----- Act & Assert -----
            dataSource.allUserLists.test {
                awaitItem() shouldBe listOf(entity.toModel())
                awaitComplete()
            }
        }
    }

    @Nested
    inner class GetBooksFlowByStatus {

        @Test
        fun `passes correct status code to DAO getBooksByStatus`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.CURRENTLY_READING
            val entity = stubBookFullEntity(id = 5)

            every {
                dao.getBooksByStatus(statusCode = status.code)
            } returns flowOf(listOf(entity))

            // ----- Act & Assert -----
            dataSource.getBooksFlowByStatus(status = status).test {
                awaitItem() shouldBe listOf(entity.toModel())
                awaitComplete()
            }
        }

        @Test
        fun `passes WANT_TO_READ status code to DAO`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.WANT_TO_READ

            every {
                dao.getBooksByStatus(statusCode = status.code)
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            dataSource.getBooksFlowByStatus(status = status).test {
                awaitItem() shouldBe emptyList<Book>()
                awaitComplete()
            }
        }

        @Test
        fun `emits mapped domain books for the given status`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.READ
            val entity1 = stubBookFullEntity(id = 10)
            val entity2 = stubBookFullEntity(id = 11)

            every {
                dao.getBooksByStatus(statusCode = status.code)
            } returns flowOf(listOf(entity1, entity2))

            // ----- Act & Assert -----
            dataSource.getBooksFlowByStatus(status = status).test {
                awaitItem() shouldBe listOf(entity1.toModel(), entity2.toModel())
                awaitComplete()
            }
        }

        @Test
        fun `suppresses consecutive duplicate emissions via distinctUntilChanged`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.PAUSED
            val entity = stubBookFullEntity(id = 3)
            val list = listOf(entity)

            every {
                dao.getBooksByStatus(statusCode = status.code)
            } returns flowOf(list, list)

            // ----- Act & Assert -----
            dataSource.getBooksFlowByStatus(status = status).test {
                awaitItem() shouldBe listOf(entity.toModel())
                awaitComplete()
            }
        }
    }

    @Nested
    inner class GetAllUserBookIds {

        @Test
        fun `delegates to DAO and returns the id list`() = runTest {
            // ----- Arrange -----
            val expectedIds = listOf(1, 2, 3)

            coEvery {
                dao.getAllUserBookIds()
            } returns expectedIds

            // ----- Act -----
            val result = dataSource.getAllUserBookIds()

            // ----- Assert -----
            result shouldBe expectedIds
        }

        @Test
        fun `returns empty list when DAO returns empty list`() = runTest {
            // ----- Arrange -----
            coEvery {
                dao.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            val result = dataSource.getAllUserBookIds()

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class GetExistingBookIds {

        @Test
        fun `returns empty list immediately when ids input is empty`() = runTest {
            // ----- Arrange -----
            // (no DAO call expected — short-circuit path)

            // ----- Act -----
            val result = dataSource.getExistingBookIds(ids = emptyList())

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 0) {
                dao.getExistingBookIds(any())
            }
        }

        @Test
        fun `delegates to DAO and returns the filtered id list when ids are non-empty`() = runTest {
            // ----- Arrange -----
            val inputIds = listOf(1, 2, 3)
            val cachedIds = listOf(1, 3)

            coEvery {
                dao.getExistingBookIds(bookIds = inputIds)
            } returns cachedIds

            // ----- Act -----
            val result = dataSource.getExistingBookIds(ids = inputIds)

            // ----- Assert -----
            result shouldBe cachedIds
        }

        @Test
        fun `returns empty list when none of the given ids exist in the database`() = runTest {
            // ----- Arrange -----
            val inputIds = listOf(10, 20)

            coEvery {
                dao.getExistingBookIds(bookIds = inputIds)
            } returns emptyList()

            // ----- Act -----
            val result = dataSource.getExistingBookIds(ids = inputIds)

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class GetExistingEditionIds {

        @Test
        fun `returns empty list immediately when ids input is empty`() = runTest {
            // ----- Arrange -----
            // (no DAO call expected — short-circuit path)

            // ----- Act -----
            val result = dataSource.getExistingEditionIds(ids = emptyList())

            // ----- Assert -----
            result shouldBe emptyList()
            coVerify(exactly = 0) {
                dao.getExistingEditionIds(any())
            }
        }

        @Test
        fun `delegates to DAO and returns the filtered id list when ids are non-empty`() = runTest {
            // ----- Arrange -----
            val inputIds = listOf(10, 20, 30)
            val cachedIds = listOf(10, 30)

            coEvery {
                dao.getExistingEditionIds(editionIds = inputIds)
            } returns cachedIds

            // ----- Act -----
            val result = dataSource.getExistingEditionIds(ids = inputIds)

            // ----- Assert -----
            result shouldBe cachedIds
        }

        @Test
        fun `returns empty list when none of the given edition ids exist in the database`() = runTest {
            // ----- Arrange -----
            val inputIds = listOf(99, 100)

            coEvery {
                dao.getExistingEditionIds(editionIds = inputIds)
            } returns emptyList()

            // ----- Act -----
            val result = dataSource.getExistingEditionIds(ids = inputIds)

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class CacheEditions {

        @Test
        fun `delegates to DAO with the given editions`() = runTest {
            // ----- Arrange -----
            val editions = listOf<BookEdition>(mockk(), mockk())

            // ----- Act -----
            dataSource.cacheEditions(editions = editions)

            // ----- Assert -----
            coVerify {
                dao.cacheEditions(editions = editions)
            }
        }

        @Test
        fun `delegates to DAO with an empty list`() = runTest {
            // ----- Arrange -----
            val editions = emptyList<BookEdition>()

            // ----- Act -----
            dataSource.cacheEditions(editions = editions)

            // ----- Assert -----
            coVerify {
                dao.cacheEditions(editions = editions)
            }
        }
    }

    @Nested
    inner class CacheBook {

        @Test
        fun `delegates to DAO with the given book`() = runTest {
            // ----- Arrange -----
            val book: Book = mockk()

            // ----- Act -----
            dataSource.cacheBook(book = book)

            // ----- Assert -----
            coVerify {
                dao.cacheBook(book = book)
            }
        }
    }

    @Nested
    inner class CacheBooks {

        @Test
        fun `delegates to DAO with the given book list`() = runTest {
            // ----- Arrange -----
            val books = listOf<Book>(mockk(), mockk())

            // ----- Act -----
            dataSource.cacheBooks(books = books)

            // ----- Assert -----
            coVerify {
                dao.cacheBooks(books = books)
            }
        }

        @Test
        fun `delegates to DAO with an empty list`() = runTest {
            // ----- Arrange -----
            val books = emptyList<Book>()

            // ----- Act -----
            dataSource.cacheBooks(books = books)

            // ----- Assert -----
            coVerify {
                dao.cacheBooks(books = books)
            }
        }
    }

    @Nested
    inner class RemoveUserBooksById {

        @Test
        fun `delegates to DAO deleteUserBooksByIds with the given ids`() = runTest {
            // ----- Arrange -----
            val ids = listOf(10, 20, 30)

            // ----- Act -----
            dataSource.removeUserBooksById(ids = ids)

            // ----- Assert -----
            coVerify {
                dao.deleteUserBooksByIds(ids)
            }
        }

        @Test
        fun `delegates to DAO with an empty id list`() = runTest {
            // ----- Arrange -----
            val ids = emptyList<Int>()

            // ----- Act -----
            dataSource.removeUserBooksById(ids = ids)

            // ----- Assert -----
            coVerify {
                dao.deleteUserBooksByIds(ids)
            }
        }
    }

    @Nested
    inner class CacheUserBookLists {

        @Test
        fun `delegates to DAO with the given lists`() = runTest {
            // ----- Arrange -----
            val lists = listOf<nl.rhaydus.softcover.core.domain.model.BookList>(mockk(), mockk())

            // ----- Act -----
            dataSource.cacheUserBookLists(lists = lists)

            // ----- Assert -----
            coVerify {
                dao.cacheBookLists(lists = lists)
            }
        }

        @Test
        fun `delegates to DAO with an empty list`() = runTest {
            // ----- Arrange -----
            val lists = emptyList<nl.rhaydus.softcover.core.domain.model.BookList>()

            // ----- Act -----
            dataSource.cacheUserBookLists(lists = lists)

            // ----- Assert -----
            coVerify {
                dao.cacheBookLists(lists = lists)
            }
        }
    }

    @Nested
    inner class CacheListBook {

        @Test
        fun `delegates to DAO with the given list book`() = runTest {
            // ----- Arrange -----
            val listBook: ListBook = mockk()

            // ----- Act -----
            dataSource.cacheListBook(book = listBook)

            // ----- Assert -----
            coVerify {
                dao.cacheListBook(listBook = listBook)
            }
        }
    }

    @Nested
    inner class RemoveAllBooks {

        @Test
        fun `delegates to DAO deleteAllUserBooksAndData`() = runTest {
            // ----- Arrange -----
            // (dao is relaxed — no additional setup needed)

            // ----- Act -----
            dataSource.removeAllBooks()

            // ----- Assert -----
            coVerify {
                dao.deleteAllUserBooksAndData()
            }
        }
    }

    @Nested
    inner class GetOwnedListBookByEditionId {

        @Test
        fun `returns mapped domain model when DAO returns a result`() = runTest {
            // ----- Arrange -----
            val editionId = 42
            val entity = stubListBookFull(
                listBookId = 1,
                listId = 1,
                bookId = 1,
                editionId = editionId,
            )

            coEvery {
                dao.getOwnedListBookByEditionId(editionId = editionId)
            } returns entity

            // ----- Act -----
            val result = dataSource.getOwnedListBookByEditionId(editionId = editionId)

            // ----- Assert -----
            result shouldBe entity.toModel()
        }

        @Test
        fun `throws Exception when DAO returns null`() = runTest {
            // ----- Arrange -----
            val editionId = 99

            coEvery {
                dao.getOwnedListBookByEditionId(editionId = editionId)
            } returns null

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                dataSource.getOwnedListBookByEditionId(editionId = editionId)
            }
        }

        @Test
        fun `forwards the editionId argument to the DAO`() = runTest {
            // ----- Arrange -----
            val editionId = 7
            val entity = stubListBookFull(
                editionId = editionId,
            )

            coEvery {
                dao.getOwnedListBookByEditionId(editionId = editionId)
            } returns entity

            // ----- Act -----
            dataSource.getOwnedListBookByEditionId(editionId = editionId)

            // ----- Assert -----
            coVerify {
                dao.getOwnedListBookByEditionId(editionId = editionId)
            }
        }
    }
}
