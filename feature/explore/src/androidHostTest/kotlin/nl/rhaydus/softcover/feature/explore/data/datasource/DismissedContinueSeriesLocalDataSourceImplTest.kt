package nl.rhaydus.softcover.feature.explore.data.datasource

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.database.dao.DismissedContinueSeriesDao
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesBookEntity
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesEntity

class DismissedContinueSeriesLocalDataSourceImplTest {
    private lateinit var dao: DismissedContinueSeriesDao
    private lateinit var dataSource: DismissedContinueSeriesLocalDataSourceImpl

    @BeforeEach
    fun setUp() {
        dao = mockk(relaxed = true)

        every {
            dao.observeDismissedBookIds()
        } returns flowOf(emptyList())
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
    inner class DismissedBookIds {
        @Test
        fun `is wired to dao observeDismissedBookIds`() = runTest {
            // ----- Arrange -----
            val expectedIds = listOf(1, 2, 3)

            every {
                dao.observeDismissedBookIds()
            } returns flowOf(expectedIds)

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

            every {
                dao.observeDismissedSeriesIds()
            } returns flowOf(expectedIds)

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
        fun `delegates to dao with correct entity including metadata`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val title = "Dune"
            val coverUrl = "https://example.com/dune.jpg"
            val authorText = "Frank Herbert"
            val seriesName = "Dune Saga"

            // ----- Act -----
            dataSource.dismissBook(
                bookId = bookId,
                title = title,
                coverUrl = coverUrl,
                authorText = authorText,
                seriesName = seriesName,
            )

            // ----- Assert -----
            coVerify {
                dao.dismissBook(
                    DismissedContinueSeriesBookEntity(
                        bookId = bookId,
                        bookTitle = title,
                        coverUrl = coverUrl,
                        authorText = authorText,
                        seriesName = seriesName,
                    ),
                )
            }
        }

        @Test
        fun `delegates to dao with null metadata when none is provided`() = runTest {
            // ----- Arrange -----
            val bookId = 43

            // ----- Act -----
            dataSource.dismissBook(
                bookId = bookId,
                title = null,
                coverUrl = null,
                authorText = null,
                seriesName = null,
            )

            // ----- Assert -----
            coVerify {
                dao.dismissBook(DismissedContinueSeriesBookEntity(bookId = bookId))
            }
        }
    }

    @Nested
    inner class DismissSeries {
        @Test
        fun `delegates to dao with correct entity including metadata`() = runTest {
            // ----- Arrange -----
            val seriesId = 99
            val seriesName = "Foundation"
            val coverUrl = "https://example.com/foundation.jpg"

            // ----- Act -----
            dataSource.dismissSeries(
                seriesId = seriesId,
                seriesName = seriesName,
                coverUrl = coverUrl,
            )

            // ----- Assert -----
            coVerify {
                dao.dismissSeries(
                    DismissedContinueSeriesEntity(
                        seriesId = seriesId,
                        seriesName = seriesName,
                        coverUrl = coverUrl,
                    ),
                )
            }
        }

        @Test
        fun `delegates to dao with null metadata when none is provided`() = runTest {
            // ----- Arrange -----
            val seriesId = 100

            // ----- Act -----
            dataSource.dismissSeries(
                seriesId = seriesId,
                seriesName = null,
                coverUrl = null,
            )

            // ----- Assert -----
            coVerify {
                dao.dismissSeries(DismissedContinueSeriesEntity(seriesId = seriesId))
            }
        }
    }

    @Nested
    inner class DismissedBooks {
        @Test
        fun `is wired to dao observeDismissedBooks`() = runTest {
            // ----- Arrange -----
            val expectedEntities = listOf(
                DismissedContinueSeriesBookEntity(
                    bookId = 1,
                    bookTitle = "Dune",
                    coverUrl = "cover.jpg",
                    authorText = "Frank Herbert",
                    seriesName = "Dune Saga",
                ),
            )

            every {
                dao.observeDismissedBooks()
            } returns flowOf(expectedEntities)

            val freshDataSource = DismissedContinueSeriesLocalDataSourceImpl(dao = dao)

            // ----- Act & Assert -----
            freshDataSource.dismissedBooks.test {
                awaitItem() shouldBe expectedEntities
                awaitComplete()
            }
        }
    }

    @Nested
    inner class DismissedSeries {
        @Test
        fun `is wired to dao observeDismissedSeries`() = runTest {
            // ----- Arrange -----
            val expectedEntities = listOf(
                DismissedContinueSeriesEntity(
                    seriesId = 10,
                    seriesName = "Foundation",
                    coverUrl = "cover.jpg",
                ),
            )

            every {
                dao.observeDismissedSeries()
            } returns flowOf(expectedEntities)

            val freshDataSource = DismissedContinueSeriesLocalDataSourceImpl(dao = dao)

            // ----- Act & Assert -----
            freshDataSource.dismissedSeries.test {
                awaitItem() shouldBe expectedEntities
                awaitComplete()
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
