package nl.rhaydus.softcover.core.database.dao

import android.content.Context
import androidx.room.Room
import app.cash.turbine.test
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.model.BookCopyEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookCopyDaoTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SoftcoverDatabase
    private lateinit var dao: BookCopyDao

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)

        database = SoftcoverDatabase.build(
            builder = Room.inMemoryDatabaseBuilder(context = context),
            queryContext = testDispatcher,
        )

        dao = database.bookCopyDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Observe {
        @Test
        fun `emits null then the row once upserted and null again after a delete`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val entity = BookCopyEntity(
                bookId = 3,
                source = "gift",
                updatedAt = 10L,
            )

            // ----- Act & Assert -----
            dao.observe(bookId = 3).test {
                awaitItem().shouldBeNull()

                dao.upsert(entity = entity)
                awaitItem() shouldBe entity

                dao.delete(bookId = 3)
                awaitItem().shouldBeNull()
            }
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `removes only the requested book`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val kept = BookCopyEntity(
                bookId = 2,
                customSource = "Book swap",
                updatedAt = 10L,
            )

            dao.upsert(
                entity = BookCopyEntity(
                    bookId = 1,
                    source = "library",
                    updatedAt = 10L,
                ),
            )

            dao.upsert(entity = kept)

            // ----- Act -----
            dao.delete(bookId = 1)

            // ----- Assert -----
            dao.get(bookId = 1).shouldBeNull()
            dao.get(bookId = 2) shouldBe kept
        }

        @Test
        fun `a row deleted then upserted again reads the new values`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val replacement = BookCopyEntity(
                bookId = 1,
                customSource = "Book swap",
                updatedAt = 20L,
            )

            dao.upsert(
                entity = BookCopyEntity(
                    bookId = 1,
                    source = "library",
                    updatedAt = 10L,
                ),
            )

            dao.delete(bookId = 1)

            // ----- Act -----
            dao.upsert(entity = replacement)

            // ----- Assert -----
            dao.get(bookId = 1) shouldBe replacement
        }
    }

    @Nested
    inner class DeleteAllLocalData {
        @Test
        fun `clearing local data empties the book copies`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.upsert(
                entity = BookCopyEntity(
                    bookId = 1,
                    source = "library",
                    updatedAt = 10L,
                ),
            )

            dao.upsert(
                entity = BookCopyEntity(
                    bookId = 2,
                    source = "gift",
                    updatedAt = 10L,
                ),
            )

            // ----- Act -----
            database.bookDao().deleteAllLocalData()

            // ----- Assert -----
            dao.get(bookId = 1).shouldBeNull()
            dao.get(bookId = 2).shouldBeNull()
        }
    }
}
