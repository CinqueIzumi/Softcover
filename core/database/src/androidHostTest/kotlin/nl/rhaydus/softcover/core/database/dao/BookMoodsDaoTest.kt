package nl.rhaydus.softcover.core.database.dao

import android.content.Context
import androidx.room.Room
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.model.BookMoodEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookMoodsDaoTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SoftcoverDatabase
    private lateinit var dao: BookMoodsDao

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)

        database = SoftcoverDatabase.build(
            builder = Room.inMemoryDatabaseBuilder(context = context),
            queryContext = testDispatcher,
        )

        dao = database.bookMoodsDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class ReplaceForBook {
        @Test
        fun `replacing a book's set leaves only the new rows`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insertAll(
                listOf(
                    BookMoodEntity(
                        bookId = 1,
                        word = "cozy",
                        isOwn = false,
                    ),
                    BookMoodEntity(
                        bookId = 1,
                        word = "bleak",
                        isOwn = false,
                    ),
                ),
            )

            val replacement = listOf(
                BookMoodEntity(
                    bookId = 1,
                    word = "tense",
                    isOwn = false,
                    strength = 3,
                ),
            )

            // ----- Act -----
            dao.replaceForBook(
                bookId = 1,
                entities = replacement,
            )

            // ----- Assert -----
            dao.observe(bookId = 1).first() shouldBe replacement
        }

        @Test
        fun `replacing a book's set leaves other books alone`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val other = BookMoodEntity(
                bookId = 2,
                word = "cozy",
                isOwn = false,
            )

            dao.insertAll(
                listOf(
                    BookMoodEntity(
                        bookId = 1,
                        word = "cozy",
                        isOwn = false,
                    ),
                    other,
                ),
            )

            // ----- Act -----
            dao.replaceForBook(
                bookId = 1,
                entities = emptyList(),
            )

            // ----- Assert -----
            dao.observe(bookId = 1).first() shouldBe emptyList()
            dao.observe(bookId = 2).first() shouldBe listOf(other)
        }
    }

    @Nested
    inner class Observe {
        @Test
        fun `an own word and a preset key with the same text coexist`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val preset = BookMoodEntity(
                bookId = 1,
                word = "cozy",
                isOwn = false,
            )

            val own = BookMoodEntity(
                bookId = 1,
                word = "cozy",
                isOwn = true,
            )

            // ----- Act -----
            dao.insertAll(listOf(preset, own))

            // ----- Assert -----
            dao.observe(bookId = 1).first().shouldContainExactlyInAnyOrder(
                preset,
                own,
            )
        }

        @Test
        fun `observing all returns rows across books`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val first = BookMoodEntity(
                bookId = 1,
                word = "cozy",
                isOwn = false,
            )

            val second = BookMoodEntity(
                bookId = 2,
                word = "bleak",
                isOwn = true,
            )

            dao.insertAll(listOf(first, second))

            // ----- Act -----
            val all = dao.observeAll().first()

            // ----- Assert -----
            all.shouldContainExactlyInAnyOrder(
                first,
                second,
            )
        }
    }
}
