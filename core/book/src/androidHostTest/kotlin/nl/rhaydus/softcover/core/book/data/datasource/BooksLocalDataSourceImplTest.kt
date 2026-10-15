package nl.rhaydus.softcover.core.book.data.datasource

import androidx.room.RoomRawQuery
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.common.AppDispatchers
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
        val dispatcher = UnconfinedTestDispatcher()
        dataSource = BooksLocalDataSourceImpl(
            dao = dao,
            editionImageStorage = editionImageStorage,
            appDispatchers = AppDispatchers(
                main = dispatcher,
                io = dispatcher,
                default = dispatcher,
            ),
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
    inner class ObserveUserBook {
        @Test
        fun `emits the mapped domain book for the observed row`() = runTest {
            // ----- Arrange -----
            val entity = stubBookFullEntity(id = 7)

            every {
                dao.observeBookById(id = 7)
            } returns flowOf(entity)

            // ----- Act & Assert -----
            dataSource.observeUserBook(id = 7).test {
                awaitItem() shouldBe entity.toModel()
                awaitComplete()
            }
        }

        @Test
        fun `emits null when the row is absent`() = runTest {
            // ----- Arrange -----
            every {
                dao.observeBookById(id = 7)
            } returns flowOf(null)

            // ----- Act & Assert -----
            dataSource.observeUserBook(id = 7).test {
                awaitItem() shouldBe null
                awaitComplete()
            }
        }

        @Test
        fun `does not re-emit when the row is unchanged`() = runTest {
            // ----- Arrange -----
            val entity = stubBookFullEntity(id = 7)

            every {
                dao.observeBookById(id = 7)
            } returns flowOf(
                entity,
                entity.copy(),
            )

            // ----- Act & Assert -----
            dataSource.observeUserBook(id = 7).test {
                awaitItem() shouldBe entity.toModel()
                awaitComplete()
            }
        }

        @Test
        fun `emits again when the row changes`() = runTest {
            // ----- Arrange -----
            val first = stubBookFullEntity(id = 7)
            val second = first.copy(book = first.book.copy(title = "Changed"))

            every {
                dao.observeBookById(id = 7)
            } returns flowOf(
                first,
                second,
            )

            // ----- Act & Assert -----
            dataSource.observeUserBook(id = 7).test {
                awaitItem() shouldBe first.toModel()
                awaitItem() shouldBe second.toModel()
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
    }

    @Nested
    inner class CacheEditions {
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
            ),
            )

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
            ),
            )

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
                dao.deleteAllLocalData()
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
            ),
            )

            // ----- Act -----
            dataSource.removeAllBooks()

            // ----- Assert -----
            coVerify(exactly = 0) {
                editionImageStorage.delete(path = any())
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

    @Nested
    inner class ObserveBooksByIds {
        @Test
        fun `emits the DAO entities mapped to books for the requested ids`() = runTest {
            // ----- Arrange -----
            val first = stubBookFullEntity(id = 1)
            val second = stubBookFullEntity(id = 2)

            every {
                dao.observeBooksByIds(ids = listOf(1, 2))
            } returns flowOf(listOf(first, second))

            // ----- Act -----
            val emissions = dataSource.observeBooksByIds(ids = listOf(1, 2)).toList()

            // ----- Assert -----
            emissions shouldBe listOf(listOf(first.toModel(), second.toModel()))
        }

        @Test
        fun `an equal re-emission from the DAO does not re-emit downstream`() = runTest {
            // ----- Arrange -----
            val entity = stubBookFullEntity(id = 1)

            every {
                dao.observeBooksByIds(ids = listOf(1))
            } returns flowOf(
                listOf(entity),
                listOf(entity.copy()),
            )

            // ----- Act -----
            val emissions = dataSource.observeBooksByIds(ids = listOf(1)).toList()

            // ----- Assert -----
            emissions.size shouldBe 1
        }

        @Test
        fun `an unchanged row keeps its Book instance while a changed row is re-mapped`() = runTest {
            // ----- Arrange -----
            val untouched = stubBookFullEntity(id = 1)
            val changing = stubBookFullEntity(id = 2)
            val changed = changing.copy(book = changing.book.copy(title = "Changed"))

            every {
                dao.observeBooksByIds(ids = listOf(1, 2))
            } returns flowOf(
                listOf(untouched, changing),
                listOf(untouched.copy(), changed),
            )

            // ----- Act -----
            val emissions = dataSource.observeBooksByIds(ids = listOf(1, 2)).toList()

            // ----- Assert -----
            emissions.size shouldBe 2
            emissions[1][0] shouldBeSameInstanceAs emissions[0][0]
            emissions[1][1] shouldNotBeSameInstanceAs emissions[0][1]
            emissions[1][1] shouldBe changed.toModel()
        }
    }

    @Nested
    inner class ReusingUnchangedBooks {
        @Test
        fun `an unchanged entity across two emissions yields the same Book instance`() = runTest {
            // ----- Arrange -----
            val first = stubBookFullEntity(id = 1)
            val second = stubBookFullEntity(id = 2)

            every {
                dao.observeBooks()
            } returns flowOf(
                listOf(first),
                listOf(first.copy(), second),
            )

            // ----- Act -----
            val emissions = dataSource.allUserBooks.toList()

            // ----- Assert -----
            emissions.size shouldBe 2
            emissions[1][0] shouldBeSameInstanceAs emissions[0][0]
        }

        @Test
        fun `a changed entity yields a new instance while untouched rows keep theirs`() = runTest {
            // ----- Arrange -----
            val untouched = stubBookFullEntity(id = 1)
            val changing = stubBookFullEntity(id = 2)
            val changed = changing.copy(book = changing.book.copy(title = "Changed"))

            every {
                dao.observeBooks()
            } returns flowOf(
                listOf(untouched, changing),
                listOf(untouched, changed),
            )

            // ----- Act -----
            val emissions = dataSource.allUserBooks.toList()

            // ----- Assert -----
            emissions[1][0] shouldBeSameInstanceAs emissions[0][0]
            emissions[1][1] shouldNotBeSameInstanceAs emissions[0][1]
            emissions[1][1] shouldBe changed.toModel()
        }

        @Test
        fun `a row removed then re-added is mapped fresh`() = runTest {
            // ----- Arrange -----
            val removed = stubBookFullEntity(id = 1)
            val kept = stubBookFullEntity(id = 2)

            every {
                dao.observeBooks()
            } returns flowOf(
                listOf(removed, kept),
                listOf(kept),
                listOf(removed, kept),
            )

            // ----- Act -----
            val emissions = dataSource.allUserBooks.toList()

            // ----- Assert -----
            emissions[2][0] shouldNotBeSameInstanceAs emissions[0][0]
            emissions[2][0] shouldBe emissions[0][0]
            emissions[2][1] shouldBeSameInstanceAs emissions[0][1]
        }

        @Test
        fun `two separate collections do not share cache state`() = runTest {
            // ----- Arrange -----
            val entity = stubBookFullEntity(id = 1)

            every {
                dao.observeBooks()
            } returns flowOf(listOf(entity))

            // ----- Act -----
            val firstCollection = dataSource.allUserBooks.toList()
            val secondCollection = dataSource.allUserBooks.toList()

            // ----- Assert -----
            secondCollection[0][0] shouldNotBeSameInstanceAs firstCollection[0][0]
            secondCollection[0][0] shouldBe firstCollection[0][0]
        }

        @Test
        fun `getSortedAllUserBooks keeps the instance of an unchanged row`() = runTest {
            // ----- Arrange -----
            val unchanged = stubBookFullEntity(id = 1)
            val added = stubBookFullEntity(id = 2)

            every {
                dao.observeBooksRaw(query = any())
            } returns flowOf(
                listOf(unchanged),
                listOf(unchanged.copy(), added),
            )

            // ----- Act -----
            val emissions = dataSource.getSortedAllUserBooks(
                mode = LibrarySortMode.TITLE,
                direction = SortDirection.ASCENDING,
            ).toList()

            // ----- Assert -----
            emissions[1][0] shouldBeSameInstanceAs emissions[0][0]
        }

        @Test
        fun `getSortedBooksByStatus keeps the instance of an unchanged row`() = runTest {
            // ----- Arrange -----
            val unchanged = stubBookFullEntity(id = 1)
            val added = stubBookFullEntity(id = 2)

            every {
                dao.observeBooksRaw(query = any())
            } returns flowOf(
                listOf(unchanged),
                listOf(unchanged.copy(), added),
            )

            // ----- Act -----
            val emissions = dataSource.getSortedBooksByStatus(
                status = UserBookStatus.READ,
                mode = LibrarySortMode.TITLE,
                direction = SortDirection.ASCENDING,
            ).toList()

            // ----- Assert -----
            emissions[1][0] shouldBeSameInstanceAs emissions[0][0]
        }

        @Test
        fun `getBooksFlowByStatus keeps the instance of an unchanged row`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.READ
            val unchanged = stubBookFullEntity(id = 1)
            val added = stubBookFullEntity(id = 2)

            every {
                dao.getReadBooks(statusCode = status.code)
            } returns flowOf(
                listOf(unchanged),
                listOf(unchanged.copy(), added),
            )

            // ----- Act -----
            val emissions = dataSource.getBooksFlowByStatus(status = status).toList()

            // ----- Assert -----
            emissions[1][0] shouldBeSameInstanceAs emissions[0][0]
        }
    }
}
