package nl.rhaydus.softcover.feature.explore.data.datasource

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.dao.DismissedContinueSeriesDao
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

        every {
            dao.observeDismissedSeriesIds()
        } returns flowOf(emptyList())
        every {
            dao.observeDismissedBooks()
        } returns flowOf(emptyList())
        every {
            dao.observeDismissedSeries()
        } returns flowOf(emptyList())

        dataSource = DismissedContinueSeriesLocalDataSourceImpl(dao = dao)
    }

    @Nested
    inner class DismissSeries {
        @Test
        fun `persists a series entity built from the given metadata`() = runTest {
            // ----- Arrange -----
            val expected = DismissedContinueSeriesEntity(
                seriesId = 99,
                seriesName = "Foundation",
                coverUrl = "https://example.com/foundation.jpg",
                authorText = "Isaac Asimov",
                bookCount = 7,
            )

            // ----- Act -----
            dataSource.dismissSeries(
                seriesId = 99,
                seriesName = "Foundation",
                coverUrl = "https://example.com/foundation.jpg",
                authorText = "Isaac Asimov",
                bookCount = 7,
            )

            // ----- Assert -----
            coVerify {
                dao.dismissSeries(expected)
            }
        }

        @Test
        fun `persists a series entity with null metadata when none is provided`() = runTest {
            // ----- Arrange -----
            val expected = DismissedContinueSeriesEntity(seriesId = 100)

            // ----- Act -----
            dataSource.dismissSeries(
                seriesId = 100,
                seriesName = null,
                coverUrl = null,
                authorText = null,
                bookCount = null,
            )

            // ----- Assert -----
            coVerify {
                dao.dismissSeries(expected)
            }
        }
    }
}
