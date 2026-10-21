package nl.rhaydus.softcover.core.database.dao

import android.content.Context
import androidx.room.Room
import app.cash.turbine.test
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.model.HiddenRecommendationEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HiddenRecommendationDaoTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SoftcoverDatabase
    private lateinit var dao: HiddenRecommendationDao

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)

        database = SoftcoverDatabase.build(
            builder = Room.inMemoryDatabaseBuilder(context = context),
            queryContext = testDispatcher,
        )
        dao = database.hiddenRecommendationDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Hide {
        @Test
        fun `a hidden book shows up in the observed flow with its metadata`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val entity = HiddenRecommendationEntity(
                bookId = 7,
                bookTitle = "Dune",
                coverUrl = "https://example.com/dune.jpg",
                authorText = "Frank Herbert",
            )

            // ----- Act -----
            dao.hide(entity)

            // ----- Assert -----
            dao.observeHiddenRecommendations().test {
                awaitItem() shouldBe listOf(entity)
            }
        }
    }

    @Nested
    inner class Restore {
        @Test
        fun `restore removes only the requested book`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.hide(HiddenRecommendationEntity(bookId = 1))
            dao.hide(HiddenRecommendationEntity(bookId = 2))

            // ----- Act -----
            dao.restore(bookId = 1)

            // ----- Assert -----
            dao.observeHiddenRecommendations().test {
                awaitItem().map { it.bookId } shouldContainExactly listOf(2)
            }
        }
    }

    @Nested
    inner class DeleteAllLocalData {
        @Test
        fun `clearing local data empties the hidden recommendations`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.hide(HiddenRecommendationEntity(bookId = 1))
            dao.hide(HiddenRecommendationEntity(bookId = 2))

            // ----- Act -----
            database.bookDao().deleteAllLocalData()

            // ----- Assert -----
            dao.observeHiddenRecommendations().test {
                awaitItem() shouldBe emptyList()
            }
        }
    }
}
