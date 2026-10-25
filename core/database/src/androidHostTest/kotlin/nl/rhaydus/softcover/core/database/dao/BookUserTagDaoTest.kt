package nl.rhaydus.softcover.core.database.dao

import android.content.Context
import androidx.room.Room
import app.cash.turbine.test
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.model.BookEntity
import nl.rhaydus.softcover.core.database.model.BookUserTagEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookUserTagDaoTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SoftcoverDatabase
    private lateinit var dao: BookUserTagDao
    private lateinit var bookDao: BookDao

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)

        database = SoftcoverDatabase.build(
            builder = Room.inMemoryDatabaseBuilder(context = context),
            queryContext = testDispatcher,
        )
        dao = database.bookUserTagDao()
        bookDao = database.bookDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    private suspend fun insertBook(id: Int) {
        bookDao.insertBook(
            book = BookEntity(
                id = id,
                title = "Book $id",
                defaultEditionId = null,
                rating = 0.0,
                headline = "",
                description = "",
                releaseYear = 2020,
                releaseDate = null,
                pages = null,
                coverUrl = "",
                usersCount = 0,
                ratingsCount = 0,
                positionsInSeries = "",
                isCompilation = false,
                seriesId = null,
            ),
        )
    }

    private fun tag(
        bookId: Int,
        name: String,
        category: String = "genre",
    ) = BookUserTagEntity(
        bookId = bookId,
        category = category,
        name = name,
        spoiler = false,
    )

    private fun tags(vararg rows: Pair<Int, String>): List<BookUserTagEntity> = rows.map { (bookId, name) ->
        tag(
            bookId = bookId,
            name = name,
        )
    }

    @Nested
    inner class ReplaceForBooks {
        @Test
        fun `replaces the rows of the given books and leaves other books alone`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertBook(id = 2)
            insertBook(id = 3)
            val seeded = tags(
                1 to "old",
                2 to "old",
                3 to "keep",
            )
            dao.insertAll(seeded)
            val replacement = tags(
                1 to "new",
                2 to "fresh",
            )

            // ----- Act -----
            dao.replaceForBooks(
                bookIds = listOf(1, 2),
                rows = replacement,
            )

            // ----- Assert -----
            dao.observeAll().test {
                awaitItem() shouldContainExactlyInAnyOrder replacement + tags(3 to "keep")
            }
        }

        @Test
        fun `a listed book without rows has its tags cleared`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertBook(id = 2)
            val seeded = tags(
                1 to "old",
                2 to "old",
            )
            dao.insertAll(seeded)

            // ----- Act -----
            dao.replaceForBooks(
                bookIds = listOf(1, 2),
                rows = tags(2 to "new"),
            )

            // ----- Assert -----
            dao.getForBook(bookId = 1).shouldBeEmpty()
            dao.getForBook(bookId = 2) shouldContainExactlyInAnyOrder tags(2 to "new")
        }
    }

    @Nested
    inner class ReplaceForBook {
        @Test
        fun `swaps one book's tags without touching another book`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertBook(id = 2)
            val seeded = tags(
                1 to "old",
                1 to "older",
                2 to "keep",
            )
            dao.insertAll(seeded)

            // ----- Act -----
            dao.replaceForBook(
                bookId = 1,
                rows = tags(1 to "new"),
            )

            // ----- Assert -----
            dao.getForBook(bookId = 1) shouldContainExactlyInAnyOrder tags(1 to "new")
            dao.getForBook(bookId = 2) shouldContainExactlyInAnyOrder tags(2 to "keep")
        }
    }

    @Nested
    inner class ObserveForBook {
        @Test
        fun `emits the new rows after a replace`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            dao.insertAll(tags(1 to "old"))

            // ----- Act & Assert -----
            dao.observeForBook(bookId = 1).test {
                awaitItem() shouldContainExactlyInAnyOrder tags(1 to "old")

                dao.replaceForBook(
                    bookId = 1,
                    rows = tags(1 to "new"),
                )

                awaitItem() shouldContainExactlyInAnyOrder tags(1 to "new")
            }
        }
    }

    @Nested
    inner class BookDeletion {
        @Test
        fun `deleting a book through the dao cascades to its tags only`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertBook(id = 2)
            val seeded = tags(
                1 to "gone",
                2 to "stays",
            )
            dao.insertAll(seeded)

            // ----- Act -----
            bookDao.deleteBooksWithChildren(bookIds = listOf(1))

            // ----- Assert -----
            dao.getForBook(bookId = 1).shouldBeEmpty()
            dao.getForBook(bookId = 2) shouldContainExactlyInAnyOrder tags(2 to "stays")
        }
    }

    @Nested
    inner class RedirectBookId {
        @Test
        fun `moves tags to the new id when that book exists`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertBook(id = 2)
            dao.insertAll(tags(1 to "moved"))

            // ----- Act -----
            bookDao.redirectBookId(
                oldId = 1,
                newId = 2,
            )

            // ----- Assert -----
            dao.getForBook(bookId = 1).shouldBeEmpty()
            dao.getForBook(bookId = 2) shouldContainExactlyInAnyOrder tags(2 to "moved")
        }

        @Test
        fun `a tag already on the new id does not crash and is not duplicated`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertBook(id = 2)
            val seeded = tags(
                1 to "shared",
                1 to "only-old",
                2 to "shared",
            )
            dao.insertAll(seeded)

            // ----- Act -----
            bookDao.redirectBookId(
                oldId = 1,
                newId = 2,
            )

            // ----- Assert -----
            dao.getForBook(bookId = 1).shouldBeEmpty()
            dao.getForBook(bookId = 2) shouldContainExactlyInAnyOrder
                tags(
                    2 to "shared",
                    2 to "only-old",
                )
        }

        @Test
        fun `leaves the tags on the old id when the new id has no book`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            dao.insertAll(tags(1 to "stays"))

            // ----- Act -----
            bookDao.redirectBookId(
                oldId = 1,
                newId = 9,
            )

            // ----- Assert -----
            dao.getForBook(bookId = 1) shouldContainExactlyInAnyOrder tags(1 to "stays")
        }
    }

    @Nested
    inner class DeleteAllLocalData {
        @Test
        fun `clears every tag row`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertBook(id = 1)
            insertBook(id = 2)
            val seeded = tags(
                1 to "a",
                2 to "b",
            )
            dao.insertAll(seeded)

            // ----- Act -----
            bookDao.deleteAllLocalData()

            // ----- Assert -----
            dao.observeAll().test {
                awaitItem().shouldBeEmpty()
            }
        }
    }
}
