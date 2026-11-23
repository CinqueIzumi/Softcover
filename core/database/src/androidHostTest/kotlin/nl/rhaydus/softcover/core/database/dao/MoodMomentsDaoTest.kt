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
import nl.rhaydus.softcover.core.database.model.MoodMomentEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MoodMomentsDaoTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SoftcoverDatabase
    private lateinit var dao: MoodMomentsDao

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)

        database = SoftcoverDatabase.build(
            builder = Room.inMemoryDatabaseBuilder(context = context),
            queryContext = testDispatcher,
        )

        dao = database.moodMomentsDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    private fun moment(
        bookId: Int,
        word: String = "cozy",
    ) = MoodMomentEntity(
        bookId = bookId,
        word = word,
        isOwn = false,
        recordedAt = 100L,
    )

    @Nested
    inner class Insert {
        @Test
        fun `inserts return increasing generated ids and the rows read back with them`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val first = moment(bookId = 1)

            val second = moment(
                bookId = 1,
                word = "bleak",
            )

            // ----- Act -----
            val firstId = dao.insert(first)
            val secondId = dao.insert(second)

            // ----- Assert -----
            (secondId > firstId) shouldBe true

            dao.observe(bookId = 1).first().shouldContainExactlyInAnyOrder(
                first.copy(id = firstId),
                second.copy(id = secondId),
            )
        }
    }

    @Nested
    inner class UpdateAndDelete {
        @Test
        fun `update rewrites the row with that id only`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val id = dao.insert(moment(bookId = 1))

            val untouched = moment(
                bookId = 1,
                word = "bleak",
            )

            val untouchedId = dao.insert(untouched)

            val edited = moment(bookId = 1).copy(
                id = id,
                strength = 4,
                percent = 50,
                page = 120,
                seconds = 30,
            )

            // ----- Act -----
            dao.update(edited)

            // ----- Assert -----
            dao.observe(bookId = 1).first().shouldContainExactlyInAnyOrder(
                edited,
                untouched.copy(id = untouchedId),
            )
        }

        @Test
        fun `delete removes only the row with that id`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val id = dao.insert(moment(bookId = 1))

            val keptId = dao.insert(
                moment(
                    bookId = 1,
                    word = "bleak",
                ),
            )

            // ----- Act -----
            dao.delete(id = id)

            // ----- Assert -----
            dao.observe(bookId = 1).first().map { it.id } shouldBe listOf(keptId)
        }
    }

    @Nested
    inner class Observe {
        @Test
        fun `observing a book excludes other books while observing all returns every row`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val oneId = dao.insert(moment(bookId = 1))
            val twoId = dao.insert(moment(bookId = 2))

            // ----- Act -----
            val forBook = dao.observe(bookId = 1).first()
            val all = dao.observeAll().first()

            // ----- Assert -----
            forBook.map { it.id } shouldBe listOf(oneId)

            all.map { it.id }.shouldContainExactlyInAnyOrder(
                oneId,
                twoId,
            )
        }
    }
}
