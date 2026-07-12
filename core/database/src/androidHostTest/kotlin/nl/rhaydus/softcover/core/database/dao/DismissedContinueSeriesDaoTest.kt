package nl.rhaydus.softcover.core.database.dao

import android.content.Context
import androidx.room.Room
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesBookEntity
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesEntity

class DismissedContinueSeriesDaoTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SoftcoverDatabase
    private lateinit var dao: DismissedContinueSeriesDao

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)

        database = SoftcoverDatabase.build(
            builder = Room.inMemoryDatabaseBuilder(context = context),
            queryContext = testDispatcher,
        )
        dao = database.dismissedContinueSeriesDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class DismissBookAndObserve {
        @Test
        fun `dismissBook persists metadata and observeDismissedBooks emits it back`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val entity = DismissedContinueSeriesBookEntity(
                bookId = 1,
                bookTitle = "Dune",
                coverUrl = "https://example.com/dune.jpg",
                authorText = "Frank Herbert",
                seriesName = "Dune Saga",
            )

            // ----- Act -----
            dao.dismissBook(entity)

            // ----- Assert -----
            dao.observeDismissedBooks().test {
                awaitItem() shouldBe listOf(entity)
            }
        }

        @Test
        fun `observeDismissedBookIds returns the bookId projection after dismissBook`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.dismissBook(
                DismissedContinueSeriesBookEntity(
                    bookId = 2,
                    bookTitle = "Neuromancer",
                ),
            )

            // ----- Act & Assert -----
            dao.observeDismissedBookIds().test {
                awaitItem() shouldBe listOf(2)
            }
        }

        @Test
        fun `undoBookDismissal removes the row`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.dismissBook(DismissedContinueSeriesBookEntity(bookId = 4))

            // ----- Act -----
            dao.undoBookDismissal(bookId = 4)

            // ----- Assert -----
            dao.observeDismissedBooks().test {
                awaitItem() shouldBe emptyList()
            }
        }
    }

    @Nested
    inner class DismissSeriesAndObserve {
        @Test
        fun `dismissSeries persists metadata and observeDismissedSeries emits it back`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val entity = DismissedContinueSeriesEntity(
                seriesId = 10,
                seriesName = "Foundation",
                coverUrl = "https://example.com/foundation.jpg",
            )

            // ----- Act -----
            dao.dismissSeries(entity)

            // ----- Assert -----
            dao.observeDismissedSeries().test {
                awaitItem() shouldBe listOf(entity)
            }
        }

        @Test
        fun `observeDismissedSeriesIds returns the seriesId projection after dismissSeries`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.dismissSeries(
                DismissedContinueSeriesEntity(
                    seriesId = 20,
                    seriesName = "Culture Series",
                ),
            )

            // ----- Act & Assert -----
            dao.observeDismissedSeriesIds().test {
                awaitItem() shouldBe listOf(20)
            }
        }

        @Test
        fun `undoSeriesDismissal removes the row`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.dismissSeries(DismissedContinueSeriesEntity(seriesId = 40))

            // ----- Act -----
            dao.undoSeriesDismissal(seriesId = 40)

            // ----- Assert -----
            dao.observeDismissedSeries().test {
                awaitItem() shouldBe emptyList()
            }
        }
    }
}
