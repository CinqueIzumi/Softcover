package nl.rhaydus.softcover.core.book.data.datasource

import androidx.room.RoomRawQuery
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.data.storage.EditionImageStorage
import nl.rhaydus.softcover.core.database.dao.BookDao
import nl.rhaydus.softcover.core.database.mapper.toModel
import nl.rhaydus.softcover.core.database.model.BookEditionEntity
import nl.rhaydus.softcover.core.database.model.BookEditionView
import nl.rhaydus.softcover.core.database.model.BookEditionWithAuthors
import nl.rhaydus.softcover.core.database.model.BookEntity
import nl.rhaydus.softcover.core.database.model.BookFullEntity
import nl.rhaydus.softcover.core.database.model.EditionImageRef
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BooksLocalDataSourceImplTest {
    private lateinit var dao: BookDao
    private lateinit var editionImageStorage: EditionImageStorage
    private lateinit var dataSource: BooksLocalDataSourceImpl

    @BeforeEach
    fun setUp() {
        dao = mockk(relaxed = true)
        editionImageStorage = mockk(relaxed = true)
        dataSource = BooksLocalDataSourceImpl(
            dao = dao,
            editionImageStorage = editionImageStorage,
        )
    }

    private fun stubBookEntity(id: Int = 1): BookEntity = BookEntity(
        id = id,
        title = "Test Book $id",
        defaultEditionId = null,
        rating = 4.0,
        headline = "",
        description = "Description",
        releaseYear = 2020,
        releaseDate = null,
        coverUrl = "https://example.com/cover.jpg",
        usersCount = 100,
        ratingsCount = 0,
        positionsInSeries = "",
        isCompilation = false,
        seriesId = null,
    )

    private fun stubEditionEntity(
        id: Int = 10,
        bookId: Int = 1,
    ): BookEditionEntity = BookEditionEntity(
        id = id,
        canonicalId = null,
        bookId = bookId,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = null,
        audioSeconds = null,
        releaseYear = 2020,
        releaseDate = null,
        format = "Paperback",
        readingFormatId = null,
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
        tags = emptyList(),
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
            } returns flowOf(
                list,
                list,
            )

            // ----- Act & Assert -----
            dataSource.allUserBooks.test {
                awaitItem() shouldBe listOf(entity.toModel())
                awaitComplete()
            }
        }
    }

    @Nested
    inner class GetBooksFlowByStatus {
        @Test
        fun `CURRENTLY_READING dispatches to getBooksByStatusAndEvents with progress_updated and user_book_read_started`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.CURRENTLY_READING
            val entity = stubBookFullEntity(id = 1)

            every {
                dao.getBooksByStatusAndEvents(
                    statusCode = status.code,
                    events = listOf("progress_updated", "user_book_read_started"),
                )
            } returns flowOf(listOf(entity))

            // ----- Act & Assert -----
            dataSource.getBooksFlowByStatus(status = status).test {
                awaitItem() shouldBe listOf(entity.toModel())
                awaitComplete()
            }
        }

        @Test
        fun `READ dispatches to getReadBooks with correct status code`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.READ
            val entity1 = stubBookFullEntity(id = 10)
            val entity2 = stubBookFullEntity(id = 11)

            every {
                dao.getReadBooks(statusCode = status.code)
            } returns flowOf(listOf(entity1, entity2))

            // ----- Act & Assert -----
            dataSource.getBooksFlowByStatus(status = status).test {
                awaitItem() shouldBe listOf(entity1.toModel(), entity2.toModel())
                awaitComplete()
            }
        }

        @Test
        fun `WANT_TO_READ dispatches to getBooksByStatusSortedByCreatedAt with correct status code`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.WANT_TO_READ

            every {
                dao.getBooksByStatusSortedByCreatedAt(statusCode = status.code)
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            dataSource.getBooksFlowByStatus(status = status).test {
                awaitItem() shouldBe emptyList<Book>()
                awaitComplete()
            }
        }

        @Test
        fun `WANT_TO_READ emits mapped books from getBooksByStatusSortedByCreatedAt`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.WANT_TO_READ
            val entity = stubBookFullEntity(id = 5)

            every {
                dao.getBooksByStatusSortedByCreatedAt(statusCode = status.code)
            } returns flowOf(listOf(entity))

            // ----- Act & Assert -----
            dataSource.getBooksFlowByStatus(status = status).test {
                awaitItem() shouldBe listOf(entity.toModel())
                awaitComplete()
            }
        }

        @Test
        fun `DID_NOT_FINISH dispatches to getBooksByStatusAndEvents with status_stopped`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.DID_NOT_FINISH
            val entity = stubBookFullEntity(id = 7)

            every {
                dao.getBooksByStatusAndEvents(
                    statusCode = status.code,
                    events = listOf("status_stopped"),
                )
            } returns flowOf(listOf(entity))

            // ----- Act & Assert -----
            dataSource.getBooksFlowByStatus(status = status).test {
                awaitItem() shouldBe listOf(entity.toModel())
                awaitComplete()
            }
        }

        @Test
        fun `suppresses consecutive duplicate emissions via distinctUntilChanged`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.CURRENTLY_READING
            val entity = stubBookFullEntity(id = 3)
            val list = listOf(entity)

            every {
                dao.getBooksByStatusAndEvents(
                    statusCode = status.code,
                    events = listOf("progress_updated", "user_book_read_started"),
                )
            } returns flowOf(
                list,
                list,
            )

            // ----- Act & Assert -----
            dataSource.getBooksFlowByStatus(status = status).test {
                awaitItem() shouldBe listOf(entity.toModel())
                awaitComplete()
            }
        }

        @Test
        fun `emits empty list when DAO emits empty list for READ status`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.READ

            every {
                dao.getReadBooks(statusCode = status.code)
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            dataSource.getBooksFlowByStatus(status = status).test {
                awaitItem() shouldBe emptyList<Book>()
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

        @Test
        fun `deletes stale cover files returned by the DAO`() = runTest {
            // ----- Arrange -----
            val editions = listOf<BookEdition>(mockk())

            coEvery {
                dao.cacheEditions(editions = editions)
            } returns listOf("p1", "p2")

            // ----- Act -----
            dataSource.cacheEditions(editions = editions)

            // ----- Assert -----
            coVerify { editionImageStorage.delete(path = "p1") }
            coVerify { editionImageStorage.delete(path = "p2") }
        }

        @Test
        fun `does not call editionImageStorage delete when DAO returns empty stale list`() = runTest {
            // ----- Arrange -----
            val editions = listOf<BookEdition>(mockk())

            coEvery {
                dao.cacheEditions(editions = editions)
            } returns emptyList()

            // ----- Act -----
            dataSource.cacheEditions(editions = editions)

            // ----- Assert -----
            coVerify(exactly = 0) { editionImageStorage.delete(path = any()) }
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

        @Test
        fun `deletes stale cover files returned by the DAO`() = runTest {
            // ----- Arrange -----
            val book: Book = mockk()

            coEvery {
                dao.cacheBook(book = book)
            } returns listOf("p1", "p2")

            // ----- Act -----
            dataSource.cacheBook(book = book)

            // ----- Assert -----
            coVerify { editionImageStorage.delete(path = "p1") }
            coVerify { editionImageStorage.delete(path = "p2") }
        }

        @Test
        fun `does not call editionImageStorage delete when DAO returns empty stale list`() = runTest {
            // ----- Arrange -----
            val book: Book = mockk()

            coEvery {
                dao.cacheBook(book = book)
            } returns emptyList()

            // ----- Act -----
            dataSource.cacheBook(book = book)

            // ----- Assert -----
            coVerify(exactly = 0) { editionImageStorage.delete(path = any()) }
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

        @Test
        fun `deletes stale cover files returned by the DAO`() = runTest {
            // ----- Arrange -----
            val books = listOf<Book>(mockk())

            coEvery {
                dao.cacheBooks(books = books)
            } returns listOf("p1", "p2")

            // ----- Act -----
            dataSource.cacheBooks(books = books)

            // ----- Assert -----
            coVerify { editionImageStorage.delete(path = "p1") }
            coVerify { editionImageStorage.delete(path = "p2") }
        }

        @Test
        fun `does not call editionImageStorage delete when DAO returns empty stale list`() = runTest {
            // ----- Arrange -----
            val books = listOf<Book>(mockk())

            coEvery {
                dao.cacheBooks(books = books)
            } returns emptyList()

            // ----- Act -----
            dataSource.cacheBooks(books = books)

            // ----- Assert -----
            coVerify(exactly = 0) { editionImageStorage.delete(path = any()) }
        }
    }

    @Nested
    inner class RemoveUserBooksById {
        @Test
        fun `delegates to DAO deleteUserBooksByIds with the given ids`() = runTest {
            // ----- Arrange -----
            val ids = listOf(10, 20, 30)

            coEvery { dao.getBookIdByUserBookId(userBookId = any()) } returns null

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

        @Test
        fun `deletes image files for each non-null localImagePath after DB removal`() = runTest {
            // ----- Arrange -----
            val userBookId = 5
            val bookId = 100
            val imagePath = "/data/user/0/edition_images/10"

            coEvery {
                dao.getBookIdByUserBookId(userBookId = userBookId)
            } returns bookId

            coEvery {
                dao.getEditionImageRefsByBookId(bookId = bookId)
            } returns listOf(EditionImageRef(
                id = 10,
                localImagePath = imagePath,
                localImageUrl = null,
            ),)

            // ----- Act -----
            dataSource.removeUserBooksById(ids = listOf(userBookId))

            // ----- Assert -----
            coVerifyOrder {
                dao.getBookIdByUserBookId(userBookId = userBookId)
                dao.getEditionImageRefsByBookId(bookId = bookId)
                dao.deleteUserBooksByIds(listOf(userBookId))
                editionImageStorage.delete(path = imagePath)
            }
        }

        @Test
        fun `does not call editionImageStorage delete for null localImagePath entries`() = runTest {
            // ----- Arrange -----
            val userBookId = 7
            val bookId = 200

            coEvery {
                dao.getBookIdByUserBookId(userBookId = userBookId)
            } returns bookId

            coEvery {
                dao.getEditionImageRefsByBookId(bookId = bookId)
            } returns listOf(EditionImageRef(
                id = 20,
                localImagePath = null,
                localImageUrl = null,
            ),)

            // ----- Act -----
            dataSource.removeUserBooksById(ids = listOf(userBookId))

            // ----- Assert -----
            coVerify(exactly = 0) {
                editionImageStorage.delete(path = any())
            }
        }

        @Test
        fun `skips editionImageStorage delete when getBookIdByUserBookId returns null`() = runTest {
            // ----- Arrange -----
            val userBookId = 9

            coEvery {
                dao.getBookIdByUserBookId(userBookId = userBookId)
            } returns null

            // ----- Act -----
            dataSource.removeUserBooksById(ids = listOf(userBookId))

            // ----- Assert -----
            coVerify(exactly = 0) {
                editionImageStorage.delete(path = any())
            }
        }
    }

    @Nested
    inner class RemoveAllBooks {
        @Test
        fun `delegates to DAO deleteAllUserBooksAndData`() = runTest {
            // ----- Arrange -----
            coEvery { dao.getAllEditionImageRefs() } returns emptyList()

            // ----- Act -----
            dataSource.removeAllBooks()

            // ----- Assert -----
            coVerify {
                dao.deleteAllUserBooksAndData()
            }
        }

        @Test
        fun `deletes image files for each non-null path after DB removal`() = runTest {
            // ----- Arrange -----
            val path1 = "/data/user/0/edition_images/1"
            val path2 = "/data/user/0/edition_images/2"

            coEvery {
                dao.getAllEditionImageRefs()
            } returns listOf(
                EditionImageRef(
                    id = 1,
                    localImagePath = path1,
                    localImageUrl = null,
                ),
                EditionImageRef(
                    id = 2,
                    localImagePath = path2,
                    localImageUrl = null,
                ),
            )

            // ----- Act -----
            dataSource.removeAllBooks()

            // ----- Assert -----
            coVerifyOrder {
                dao.getAllEditionImageRefs()
                dao.deleteAllUserBooksAndData()
                editionImageStorage.delete(path = path1)
                editionImageStorage.delete(path = path2)
            }
        }

        @Test
        fun `does not call editionImageStorage delete for null localImagePath entries`() = runTest {
            // ----- Arrange -----
            coEvery {
                dao.getAllEditionImageRefs()
            } returns listOf(EditionImageRef(
                id = 3,
                localImagePath = null,
                localImageUrl = null,
            ),)

            // ----- Act -----
            dataSource.removeAllBooks()

            // ----- Assert -----
            coVerify(exactly = 0) {
                editionImageStorage.delete(path = any())
            }
        }
    }

    @Nested
    inner class UpdateEditionLocalImage {
        @Test
        fun `delegates to DAO with the given editionId, path, and url`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val path = "/data/user/0/edition_images/10"
            val url = "https://example.com/cover.jpg"

            // ----- Act -----
            dataSource.updateEditionLocalImage(
                editionId = editionId,
                path = path,
                url = url,
            )

            // ----- Assert -----
            coVerify {
                dao.updateEditionLocalImage(
                    editionId = editionId,
                    path = path,
                    url = url,
                )
            }
        }

        @Test
        fun `delegates to DAO with a null path and null url`() = runTest {
            // ----- Arrange -----
            val editionId = 20

            // ----- Act -----
            dataSource.updateEditionLocalImage(
                editionId = editionId,
                path = null,
                url = null,
            )

            // ----- Assert -----
            coVerify {
                dao.updateEditionLocalImage(
                    editionId = editionId,
                    path = null,
                    url = null,
                )
            }
        }
    }

    @Nested
    inner class PersistEditionImage {
        @Test
        fun `writes bytes and updates the DAO with stored path and url`() = runTest {
            // ----- Arrange -----
            val editionId = 55
            val url = "https://example.com/cover55.jpg"
            val bytes = byteArrayOf(4, 5, 6)
            val storedPath = "/data/user/0/edition_images/55"

            every {
                editionImageStorage.write(
                    editionId = editionId,
                    bytes = bytes,
                )
            } returns storedPath

            // ----- Act -----
            dataSource.persistEditionImage(
                editionId = editionId,
                url = url,
                bytes = bytes,
            )

            // ----- Assert -----
            coVerify {
                editionImageStorage.write(
                    editionId = editionId,
                    bytes = bytes,
                )
            }

            coVerify {
                dao.updateEditionLocalImage(
                    editionId = editionId,
                    path = storedPath,
                    url = url,
                )
            }
        }
    }

    @Nested
    inner class ReplaceShelfManualOrder {
        @Test
        fun `forwards status code and bookIds to the DAO`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.READ
            val bookIds = listOf(3, 1, 2)

            // ----- Act -----
            dataSource.applyShelfManualOrderPrefix(
                status = status,
                prefixBookIds = bookIds,
            )

            // ----- Assert -----
            coVerify {
                dao.applyShelfManualOrderPrefix(
                    statusCode = status.code,
                    prefixBookIds = bookIds,
                )
            }
        }

        @Test
        fun `forwards status code for a different status`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.WANT_TO_READ
            val bookIds = emptyList<Int>()

            // ----- Act -----
            dataSource.applyShelfManualOrderPrefix(
                status = status,
                prefixBookIds = bookIds,
            )

            // ----- Assert -----
            coVerify {
                dao.applyShelfManualOrderPrefix(
                    statusCode = status.code,
                    prefixBookIds = bookIds,
                )
            }
        }
    }

    @Nested
    inner class RemoveUserBooksByIdWithManualOrder {
        @Test
        fun `calls deleteShelfManualOrderForBookIds with resolved bookIds when bookIds is non-empty`() = runTest {
            // ----- Arrange -----
            val userBookId = 5
            val bookId = 100

            coEvery {
                dao.getBookIdByUserBookId(userBookId = userBookId)
            } returns bookId

            coEvery {
                dao.getEditionImageRefsByBookId(bookId = bookId)
            } returns emptyList()

            // ----- Act -----
            dataSource.removeUserBooksById(ids = listOf(userBookId))

            // ----- Assert -----
            coVerify {
                dao.deleteShelfManualOrderForBookIds(bookIds = listOf(bookId))
            }
        }

        @Test
        fun `does not call deleteShelfManualOrderForBookIds when all userBookId lookups return null`() = runTest {
            // ----- Arrange -----
            val userBookId = 9

            coEvery {
                dao.getBookIdByUserBookId(userBookId = userBookId)
            } returns null

            // ----- Act -----
            dataSource.removeUserBooksById(ids = listOf(userBookId))

            // ----- Assert -----
            coVerify(exactly = 0) {
                dao.deleteShelfManualOrderForBookIds(bookIds = any())
            }
        }
    }

    @Nested
    inner class GetSortedBooksByStatus {
        @Test
        fun `MANUAL mode builds SQL containing shelf_manual_order and binds statusCode twice`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.READ
            val querySlot = slot<RoomRawQuery>()

            every {
                dao.observeBooksRaw(query = capture(querySlot))
            } returns flowOf(emptyList())

            // ----- Act -----
            dataSource.getSortedBooksByStatus(
                status = status,
                mode = LibrarySortMode.MANUAL,
                direction = SortDirection.ASCENDING,
            ).test {
                awaitItem()
                awaitComplete()
            }

            // ----- Assert -----
            val sql = querySlot.captured.sql
            sql shouldContain "shelf_manual_order"
        }

        @Test
        fun `non-MANUAL mode builds SQL without shelf_manual_order and binds statusCode once`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.READ
            val querySlot = slot<RoomRawQuery>()

            every {
                dao.observeBooksRaw(query = capture(querySlot))
            } returns flowOf(emptyList())

            // ----- Act -----
            dataSource.getSortedBooksByStatus(
                status = status,
                mode = LibrarySortMode.TITLE,
                direction = SortDirection.ASCENDING,
            ).test {
                awaitItem()
                awaitComplete()
            }

            // ----- Assert -----
            val sql = querySlot.captured.sql
            sql shouldNotContain "shelf_manual_order"
        }
    }
}
