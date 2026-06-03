package nl.rhaydus.softcover.feature.explore.data.datasource

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.dao.DismissedContinueSeriesDao
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesBookEntity
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DismissedContinueSeriesLocalDataSourceImplTest {
    private lateinit var dao: DismissedContinueSeriesDao
    private lateinit var dataSource: DismissedContinueSeriesLocalDataSourceImpl

    @BeforeEach
    fun setUp() {
        dao = mockk(relaxed = true)

        every { dao.observeDismissedBookIds() } returns flowOf(emptyList())
        every { dao.observeDismissedSeriesIds() } returns flowOf(emptyList())

        dataSource = DismissedContinueSeriesLocalDataSourceImpl(dao = dao)
    }

    @Nested
    inner class DismissedBookIds {
        @Test
        fun `is wired to dao observeDismissedBookIds`() = runTest {
            // ----- Arrange -----
            val expectedIds = listOf(1, 2, 3)

            every { dao.observeDismissedBookIds() } returns flowOf(expectedIds)

            val freshDataSource = DismissedContinueSeriesLocalDataSourceImpl(dao = dao)

            // ----- Act & Assert -----
            freshDataSource.dismissedBookIds.test {
                awaitItem() shouldBe expectedIds
                awaitComplete()
            }
        }
    }

    @Nested
    inner class DismissedSeriesIds {
        @Test
        fun `is wired to dao observeDismissedSeriesIds`() = runTest {
            // ----- Arrange -----
            val expectedIds = listOf(10, 20)

            every { dao.observeDismissedSeriesIds() } returns flowOf(expectedIds)

            val freshDataSource = DismissedContinueSeriesLocalDataSourceImpl(dao = dao)

            // ----- Act & Assert -----
            freshDataSource.dismissedSeriesIds.test {
                awaitItem() shouldBe expectedIds
                awaitComplete()
            }
        }
    }

    @Nested
    inner class DismissBook {
        @Test
        fun `delegates to dao with correct entity`() = runTest {
            // ----- Arrange -----
            val bookId = 42

            // ----- Act -----
            dataSource.dismissBook(bookId = bookId)

            // ----- Assert -----
            coVerify {
                dao.dismissBook(DismissedContinueSeriesBookEntity(bookId = bookId))
            }
        }
    }

    @Nested
    inner class DismissSeries {
        @Test
        fun `delegates to dao with correct entity`() = runTest {
            // ----- Arrange -----
            val seriesId = 99

            // ----- Act -----
            dataSource.dismissSeries(seriesId = seriesId)

            // ----- Assert -----
            coVerify {
                dao.dismissSeries(DismissedContinueSeriesEntity(seriesId = seriesId))
            }
        }
    }

    @Nested
    inner class UndoBookDismissal {
        @Test
        fun `delegates to dao with correct bookId`() = runTest {
            // ----- Arrange -----
            val bookId = 77

            // ----- Act -----
            dataSource.undoBookDismissal(bookId = bookId)

            // ----- Assert -----
            coVerify {
                dao.undoBookDismissal(bookId = bookId)
            }
        }
    }

    @Nested
    inner class UndoSeriesDismissal {
        @Test
        fun `delegates to dao with correct seriesId`() = runTest {
            // ----- Arrange -----
            val seriesId = 55

            // ----- Act -----
            dataSource.undoSeriesDismissal(seriesId = seriesId)

            // ----- Assert -----
            coVerify {
                dao.undoSeriesDismissal(seriesId = seriesId)
            }
        }
    }
}
