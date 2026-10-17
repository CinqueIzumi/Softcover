package nl.rhaydus.softcover.core.database.dao

import android.content.Context
import androidx.room.Room
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.mapper.toEntity
import nl.rhaydus.softcover.core.database.mapper.toModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookDaoCacheBookReadsTest {
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

    private fun read(
        id: Int,
        currentPage: Int? = null,
    ) = UserBookRead(
        id = id,
        currentPage = currentPage,
        currentSeconds = null,
        progress = 0f,
        startedAt = null,
        finishedAt = null,
    )

    private fun book(
        id: Int,
        userBookId: Int,
        reads: List<UserBookRead>,
    ) = Book(
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
        userBook = UserBook(
            id = userBookId,
            status = BookStatus.Reading,
            dateAdded = "2024-01-01",
            createdAt = null,
            privacySettingId = 1,
            reviewHasSpoilers = false,
            editionId = null,
            lastReadDate = null,
            rating = null,
            referrerUserId = null,
            reviewedAt = null,
            updatedAt = null,
            journals = emptyList(),
            reads = reads,
        ),
    )

    private suspend fun readIdsOf(bookId: Int): List<Int> =
        dao.observeBookById(id = bookId).first()
            .shouldNotBeNull()
            .let { entity -> entity.userBookWithJournals.shouldNotBeNull().userBookReads }
            .map { it.id }

    // The replace-all lives in `cacheBook` as deleteUserBookRead + insertUserBookReads. Re-running the
    // whole `cacheBook` for an existing book needs Room's @Upsert conflict path (it upserts the book
    // row), which cannot run under the sqlite-bundled-jvm host binary (its uniqueness error carries a
    // null message), so the replace-all steps are driven directly after a first cacheBook.
    @Nested
    inner class CacheBook {
        @Test
        fun `stores every read of the user book`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val cached = book(
                id = 1,
                userBookId = 10,
                reads = listOf(read(id = 1), read(id = 2)),
            )

            // ----- Act -----
            dao.cacheBook(book = cached)

            // ----- Assert -----
            readIdsOf(bookId = 1).sorted() shouldContainExactly listOf(1, 2)
        }
    }

    @Nested
    inner class ReplacingReads {
        private suspend fun replaceReads(
            userBookId: Int,
            reads: List<UserBookRead>,
        ) {
            dao.deleteUserBookRead(userBookId = userBookId)
            dao.insertUserBookReads(userBookReads = reads.map { it.toEntity(userBookId = userBookId) })
        }

        @Test
        fun `replacing reads 1 and 2 with reads 2 and 3 leaves exactly 2 and 3`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.cacheBook(book = book(
                id = 1,
                userBookId = 10,
                reads = listOf(read(id = 1), read(id = 2)),
            ),)

            // ----- Act -----
            replaceReads(
                userBookId = 10,
                reads = listOf(read(id = 2), read(id = 3)),
            )

            // ----- Assert -----
            readIdsOf(bookId = 1).sorted() shouldContainExactly listOf(2, 3)
        }

        @Test
        fun `replacing with no reads removes every stored read`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.cacheBook(book = book(
                id = 1,
                userBookId = 10,
                reads = listOf(read(id = 1), read(id = 2)),
            ),)

            // ----- Act -----
            replaceReads(
                userBookId = 10,
                reads = emptyList(),
            )

            // ----- Assert -----
            readIdsOf(bookId = 1).shouldBeEmpty()
        }

        @Test
        fun `a kept read takes the new values`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.cacheBook(book = book(
                id = 1,
                userBookId = 10,
                reads = listOf(read(
                    id = 2,
                    currentPage = 10,
                ),),
            ),)

            // ----- Act -----
            replaceReads(
                userBookId = 10,
                reads = listOf(read(
                    id = 2,
                    currentPage = 99,
                ),),
            )

            // ----- Assert -----
            dao.observeBookById(id = 1).first().shouldNotBeNull()
                .userBookWithJournals.shouldNotBeNull()
                .userBookReads.map { it.currentPage } shouldContainExactly listOf(99)
        }

        @Test
        fun `another user book's reads are untouched`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.cacheBook(book = book(
                id = 1,
                userBookId = 10,
                reads = listOf(read(id = 1), read(id = 2)),
            ),)
            dao.cacheBook(book = book(
                id = 2,
                userBookId = 20,
                reads = listOf(read(id = 7), read(id = 8)),
            ),)

            // ----- Act -----
            replaceReads(
                userBookId = 10,
                reads = listOf(read(id = 3)),
            )

            // ----- Assert -----
            readIdsOf(bookId = 1).sorted() shouldContainExactly listOf(3)
            readIdsOf(bookId = 2).sorted() shouldContainExactly listOf(7, 8)
        }
    }

    @Nested
    inner class ObservedReads {
        @Test
        fun `the observed book's reads come back sorted by id regardless of insertion order`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.cacheBook(
                book = book(
                    id = 1,
                    userBookId = 10,
                    reads = listOf(read(id = 9), read(id = -1), read(id = 4)),
                ),
            )

            // ----- Act -----
            val reads = dao.observeBookById(id = 1).first().shouldNotBeNull().toModel().userBook.shouldNotBeNull().reads.map { it.id }

            // ----- Assert -----
            reads shouldContainExactly listOf(-1, 4, 9)
        }
    }
}
