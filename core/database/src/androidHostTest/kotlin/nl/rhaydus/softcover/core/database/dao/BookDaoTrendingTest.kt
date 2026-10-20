package nl.rhaydus.softcover.core.database.dao

import android.content.Context
import androidx.room.Room
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.model.BookEntity
import nl.rhaydus.softcover.core.database.model.ListBookEntity
import nl.rhaydus.softcover.core.database.model.TrendingBookEntity
import nl.rhaydus.softcover.core.database.model.TrendingMetaEntity
import nl.rhaydus.softcover.core.database.model.UserBookEntity
import nl.rhaydus.softcover.core.domain.model.Book
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookDaoTrendingTest {
    private companion object {
        const val WEEK = "week"
        const val MONTH = "month"
    }

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SoftcoverDatabase
    private lateinit var dao: BookDao

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)

        database = SoftcoverDatabase.build(
            builder = Room.inMemoryDatabaseBuilder(context = context),
            queryContext = testDispatcher,
        )
        dao = database.bookDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    private suspend fun insertBook(id: Int) {
        dao.insertBook(
            book = BookEntity(
                id = id,
                title = "Book $id",
                defaultEditionId = null,
                rating = 0.0,
                headline = "",
                description = "",
                releaseYear = 2020,
                releaseDate = null,
                coverUrl = "",
                usersCount = 0,
                ratingsCount = 0,
                positionsInSeries = "",
                isCompilation = false,
                seriesId = null,
            ),
        )
    }

    private fun domainBook(id: Int): Book = Book(
        id = id,
        title = "Book $id",
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = null,
    )

    private suspend fun insertUserBook(
        userBookId: Int,
        bookId: Int,
    ) {
        dao.insertUserBook(
            userBook = UserBookEntity(
                id = userBookId,
                bookId = bookId,
                statusCode = 1,
                dateAdded = "2024-01-01",
                privacySettingId = 1,
                reviewHasSpoilers = false,
                editionId = null,
                lastReadDate = null,
                rating = null,
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                createdAt = null,
            ),
        )
    }

    private suspend fun insertListBook(bookId: Int) {
        dao.insertListBook(
            listBook = ListBookEntity(
                listBookId = bookId,
                listId = 1,
                bookId = bookId,
                editionId = bookId,
                position = null,
            ),
        )
    }

    private suspend fun trendingIds(period: String = WEEK): List<Int> =
        dao.observeTrendingBooks(period = period).first().map { it.book.id }

    private suspend fun libraryIds(): List<Int> = dao.observeBooks().first().map { it.book.id }

    @Nested
    inner class ObserveBooks {
        @Test
        fun `excludes books referenced only by trending`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertBook(id = 2)
            insertUserBook(
                userBookId = 10,
                bookId = 1,
            )
            dao.upsertTrending(
                rows = listOf(
                    TrendingBookEntity(
                        period = WEEK,
                        bookId = 2,
                        rank = 0,
                    ),
                ),
            )

            // ----- Act -----
            val ids = libraryIds()

            // ----- Assert -----
            ids shouldContainExactly listOf(1)
        }

        @Test
        fun `includes library books and list-only books`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertBook(id = 2)
            insertUserBook(
                userBookId = 10,
                bookId = 1,
            )
            insertListBook(bookId = 2)

            // ----- Act -----
            val ids = libraryIds()

            // ----- Assert -----
            ids shouldContainExactlyInAnyOrder listOf(1, 2)
        }
    }

    @Nested
    inner class ReplaceTrendingBooks {
        @Test
        fun `caches the books, ranks them in requested order and records the fetch together`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val books = listOf(
                    domainBook(id = 1),
                    domainBook(id = 2),
                    domainBook(id = 3),
                )

                // ----- Act -----
                dao.replaceTrendingBooks(
                    period = WEEK,
                    requestedIds = listOf(3, 1, 2),
                    books = books,
                    fetchedAtEpochMillis = 200L,
                )

                // ----- Assert -----
                trendingIds() shouldContainExactly listOf(3, 1, 2)
                dao.getTrendingFetchedAt(period = WEEK) shouldBe 200L
            }

        @Test
        fun `returns the ids by rank rather than by book id`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val books = listOf(
                domainBook(id = 10),
                domainBook(id = 20),
                domainBook(id = 30),
            )
            dao.replaceTrendingBooks(
                period = WEEK,
                requestedIds = listOf(30, 10, 20),
                books = books,
                fetchedAtEpochMillis = 200L,
            )

            // ----- Act -----
            val ids = dao.getTrendingBookIds(period = WEEK)

            // ----- Assert -----
            ids shouldContainExactly listOf(30, 10, 20)
        }

        @Test
        fun `appends a book returned under a redirected id after the requested ones`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val books = listOf(
                domainBook(id = 1),
                domainBook(id = 9),
            )

            // ----- Act -----
            dao.replaceTrendingBooks(
                period = WEEK,
                requestedIds = listOf(1, 4),
                books = books,
                fetchedAtEpochMillis = 200L,
            )

            // ----- Assert -----
            trendingIds() shouldContainExactly listOf(1, 9)
        }

        @Test
        fun `deletes a book that left trending and is no longer referenced`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            dao.upsertTrending(
                rows = listOf(
                    TrendingBookEntity(
                        period = WEEK,
                        bookId = 1,
                        rank = 0,
                    ),
                ),
            )

            // ----- Act -----
            dao.replaceTrendingBooks(
                period = WEEK,
                requestedIds = listOf(2),
                books = listOf(domainBook(id = 2)),
                fetchedAtEpochMillis = 200L,
            )

            // ----- Assert -----
            dao.getBookById(id = 1).shouldBeNull()
            dao.getBookById(id = 2).shouldNotBeNull()
            trendingIds() shouldContainExactly listOf(2)
        }

        @Test
        fun `keeps a book that left trending but is in the library or on a list`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertBook(id = 2)
            insertUserBook(
                userBookId = 10,
                bookId = 1,
            )
            insertListBook(bookId = 2)
            dao.upsertTrending(
                rows = listOf(
                    TrendingBookEntity(
                        period = WEEK,
                        bookId = 1,
                        rank = 0,
                    ),
                    TrendingBookEntity(
                        period = WEEK,
                        bookId = 2,
                        rank = 1,
                    ),
                ),
            )

            // ----- Act -----
            dao.replaceTrendingBooks(
                period = WEEK,
                requestedIds = listOf(3),
                books = listOf(domainBook(id = 3)),
                fetchedAtEpochMillis = 200L,
            )

            // ----- Assert -----
            dao.getBookById(id = 1).shouldNotBeNull()
            dao.getBookById(id = 2).shouldNotBeNull()
        }

        @Test
        fun `does not delete an unreferenced book that was never trending`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 7)

            // ----- Act -----
            dao.replaceTrendingBooks(
                period = WEEK,
                requestedIds = listOf(1),
                books = listOf(domainBook(id = 1)),
                fetchedAtEpochMillis = 200L,
            )

            // ----- Assert -----
            dao.getBookById(id = 7).shouldNotBeNull()
        }

        @Test
        fun `leaves the other periods' rows and fetch time untouched`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.replaceTrendingBooks(
                period = MONTH,
                requestedIds = listOf(5, 6),
                books = listOf(domainBook(id = 5), domainBook(id = 6)),
                fetchedAtEpochMillis = 100L,
            )

            // ----- Act -----
            dao.replaceTrendingBooks(
                period = WEEK,
                requestedIds = listOf(1),
                books = listOf(domainBook(id = 1)),
                fetchedAtEpochMillis = 200L,
            )

            // ----- Assert -----
            trendingIds(period = MONTH) shouldContainExactly listOf(5, 6)
            dao.getTrendingFetchedAt(period = MONTH) shouldBe 100L
            trendingIds(period = WEEK) shouldContainExactly listOf(1)
            dao.getTrendingFetchedAt(period = WEEK) shouldBe 200L
        }

        @Test
        fun `only cleans up books that left the replaced period`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertBook(id = 5)
            dao.upsertTrending(
                rows = listOf(
                    TrendingBookEntity(
                        period = WEEK,
                        bookId = 1,
                        rank = 0,
                    ),
                    TrendingBookEntity(
                        period = MONTH,
                        bookId = 5,
                        rank = 0,
                    ),
                ),
            )
            insertBook(id = 7)

            // ----- Act -----
            dao.replaceTrendingBooks(
                period = WEEK,
                requestedIds = listOf(2),
                books = listOf(domainBook(id = 2)),
                fetchedAtEpochMillis = 200L,
            )

            // ----- Assert -----
            dao.getBookById(id = 1).shouldBeNull()
            dao.getBookById(id = 5).shouldNotBeNull()
            dao.getBookById(id = 7).shouldNotBeNull()
        }

        @Test
        fun `keeps a book that left one period but still trends in another`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            dao.upsertTrending(
                rows = listOf(
                    TrendingBookEntity(
                        period = WEEK,
                        bookId = 1,
                        rank = 0,
                    ),
                    TrendingBookEntity(
                        period = MONTH,
                        bookId = 1,
                        rank = 0,
                    ),
                ),
            )

            // ----- Act -----
            dao.replaceTrendingBooks(
                period = WEEK,
                requestedIds = listOf(2),
                books = listOf(domainBook(id = 2)),
                fetchedAtEpochMillis = 200L,
            )

            // ----- Assert -----
            dao.getBookById(id = 1).shouldNotBeNull()
            trendingIds(period = MONTH) shouldContainExactly listOf(1)
            trendingIds(period = WEEK) shouldContainExactly listOf(2)
        }
    }

    @Nested
    inner class OrphanCleanup {
        @Test
        fun `does not treat a trending book as an orphan`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertBook(id = 2)
            dao.upsertTrending(
                rows = listOf(
                    TrendingBookEntity(
                        period = WEEK,
                        bookId = 1,
                        rank = 0,
                    ),
                ),
            )

            // ----- Act -----
            val orphans = dao.getOrphanBookIds()
            dao.deleteOrphanBooks()

            // ----- Assert -----
            orphans shouldContainExactly listOf(2)
            dao.getBookById(id = 1).shouldNotBeNull()
            dao.getBookById(id = 2).shouldBeNull()
        }
    }

    @Nested
    inner class RedirectBookId {
        @Test
        fun `moves a trending row to the new id keeping its rank`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertBook(id = 2)
            insertBook(id = 3)
            dao.upsertTrending(
                rows = listOf(
                    TrendingBookEntity(
                        period = WEEK,
                        bookId = 3,
                        rank = 0,
                    ),
                    TrendingBookEntity(
                        period = WEEK,
                        bookId = 1,
                        rank = 1,
                    ),
                ),
            )

            // ----- Act -----
            dao.redirectBookId(
                oldId = 1,
                newId = 2,
            )

            // ----- Assert -----
            trendingIds() shouldContainExactly listOf(3, 2)
        }

        @Test
        fun `drops the old trending rows and keeps the user book when the new id has no book`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                insertBook(id = 1)
                insertUserBook(
                    userBookId = 10,
                    bookId = 1,
                )
                dao.upsertTrending(
                    rows = listOf(
                        TrendingBookEntity(
                            period = WEEK,
                            bookId = 1,
                            rank = 0,
                        ),
                        TrendingBookEntity(
                            period = MONTH,
                            bookId = 1,
                            rank = 0,
                        ),
                    ),
                )

                // ----- Act -----
                dao.redirectBookId(
                    oldId = 1,
                    newId = 2,
                )

                // ----- Assert -----
                trendingIds(period = WEEK) shouldContainExactly emptyList()
                trendingIds(period = MONTH) shouldContainExactly emptyList()
                dao.getBookIdByUserBookId(userBookId = 10) shouldBe 1
            }

        @Test
        fun `moves the trending rows of every period to an existing new book`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertBook(id = 2)
            dao.upsertTrending(
                rows = listOf(
                    TrendingBookEntity(
                        period = WEEK,
                        bookId = 1,
                        rank = 0,
                    ),
                    TrendingBookEntity(
                        period = MONTH,
                        bookId = 1,
                        rank = 3,
                    ),
                ),
            )

            // ----- Act -----
            dao.redirectBookId(
                oldId = 1,
                newId = 2,
            )

            // ----- Assert -----
            trendingIds(period = WEEK) shouldContainExactly listOf(2)
            trendingIds(period = MONTH) shouldContainExactly listOf(2)
        }
    }

    @Nested
    inner class DeleteAllForUserBookId {
        @Test
        fun `keeps a book that is still trending`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertUserBook(
                userBookId = 10,
                bookId = 1,
            )
            dao.upsertTrending(
                rows = listOf(
                    TrendingBookEntity(
                        period = WEEK,
                        bookId = 1,
                        rank = 0,
                    ),
                ),
            )

            // ----- Act -----
            dao.deleteAllForUserBookId(userBookId = 10)

            // ----- Assert -----
            trendingIds() shouldContainExactly listOf(1)
            dao.getBookIdByUserBookId(userBookId = 10).shouldBeNull()
        }

        @Test
        fun `deletes a book that is not trending`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertUserBook(
                userBookId = 10,
                bookId = 1,
            )

            // ----- Act -----
            dao.deleteAllForUserBookId(userBookId = 10)

            // ----- Assert -----
            dao.getBookById(id = 1).shouldBeNull()
        }
    }

    @Nested
    inner class DeleteAllLocalData {
        @Test
        fun `empties both trending tables`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            dao.upsertTrending(
                rows = listOf(
                    TrendingBookEntity(
                        period = WEEK,
                        bookId = 1,
                        rank = 0,
                    ),
                ),
            )

            dao.upsertTrendingMeta(
                meta = TrendingMetaEntity(
                    period = WEEK,
                    fetchedAtEpochMillis = 100L,
                ),
            )

            // ----- Act -----
            dao.deleteAllLocalData()

            // ----- Assert -----
            trendingIds() shouldContainExactly emptyList()
            dao.getTrendingFetchedAt(period = WEEK).shouldBeNull()
        }
    }
}
