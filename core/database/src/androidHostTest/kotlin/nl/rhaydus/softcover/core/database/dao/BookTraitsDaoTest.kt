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
import nl.rhaydus.softcover.core.database.model.BookTraitsEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookTraitsDaoTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SoftcoverDatabase
    private lateinit var dao: BookTraitsDao

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)

        database = SoftcoverDatabase.build(
            builder = Room.inMemoryDatabaseBuilder(context = context),
            queryContext = testDispatcher,
        )
        dao = database.bookTraitsDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Observe {
        @Test
        fun `a book without a row emits null and then the row once upserted`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val entity = BookTraitsEntity(
                bookId = 3,
                pace = "fast",
                vivid = true,
                updatedAt = 10L,
            )

            // ----- Act & Assert -----
            dao.observe(bookId = 3).test {
                awaitItem().shouldBeNull()

                dao.upsert(entity = entity)

                awaitItem() shouldBe entity
            }
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `delete removes only the requested book`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val deleted = BookTraitsEntity(
                bookId = 1,
                updatedAt = 10L,
            )
            val kept = BookTraitsEntity(
                bookId = 2,
                updatedAt = 10L,
            )
            dao.upsert(entity = deleted)
            dao.upsert(entity = kept)

            // ----- Act -----
            dao.delete(bookId = 1)

            // ----- Assert -----
            dao.get(bookId = 1).shouldBeNull()
            dao.get(bookId = 2) shouldBe kept
        }
    }

    @Nested
    inner class DeleteAllLocalData {
        @Test
        fun `clearing local data empties the book traits`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val first = BookTraitsEntity(
                bookId = 1,
                updatedAt = 10L,
            )
            val second = BookTraitsEntity(
                bookId = 2,
                updatedAt = 10L,
            )
            dao.upsert(entity = first)
            dao.upsert(entity = second)

            // ----- Act -----
            database.bookDao().deleteAllLocalData()

            // ----- Assert -----
            dao.get(bookId = 1).shouldBeNull()
            dao.get(bookId = 2).shouldBeNull()
        }
    }
}
