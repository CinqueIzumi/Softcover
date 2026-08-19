package nl.rhaydus.softcover.feature.settings.data.datasource

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.database.dao.RoadmapDocumentDao
import nl.rhaydus.softcover.core.database.model.RoadmapDocumentEntity
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSource

class RoadmapLocalDataSourceImplTest {
    private lateinit var dao: RoadmapDocumentDao
    private lateinit var dataSource: RoadmapLocalDataSourceImpl

    @BeforeEach
    fun setUp() {
        dao = mockk(relaxed = true)
        dataSource = RoadmapLocalDataSourceImpl(dao = dao)
    }

    private fun stubEntity(markdown: String = "# Roadmap"): RoadmapDocumentEntity = RoadmapDocumentEntity(
        markdown = markdown,
        fetchedAtEpochMillis = 1_000L,
    )

    @Nested
    inner class ObserveCachedRoadmap {
        @Test
        fun `maps the dao entity to a RoadmapDocument sourced from CACHE`() = runTest {
            // ----- Arrange -----
            val entity = stubEntity()

            every {
                dao.observeRoadmapDocument()
            } returns flowOf(entity)

            // ----- Act & Assert -----
            dataSource.observeCachedRoadmap().test {
                val document = awaitItem()
                document?.source shouldBe RoadmapSource.CACHE
                document?.fetchedAtEpochMillis shouldBe entity.fetchedAtEpochMillis
                awaitComplete()
            }
        }

        @Test
        fun `passes through null when the dao has no cached row`() = runTest {
            // ----- Arrange -----
            every {
                dao.observeRoadmapDocument()
            } returns flowOf(null)

            // ----- Act & Assert -----
            dataSource.observeCachedRoadmap().test {
                awaitItem() shouldBe null
                awaitComplete()
            }
        }
    }

    @Nested
    inner class GetCachedAtEpochMillis {
        @Test
        fun `delegates to dao getFetchedAtEpochMillis`() = runTest {
            // ----- Arrange -----
            coEvery {
                dao.getFetchedAtEpochMillis()
            } returns 1_000L

            // ----- Act -----
            val result = dataSource.getCachedAtEpochMillis()

            // ----- Assert -----
            result shouldBe 1_000L
        }

        @Test
        fun `returns null when the dao has no cached row`() = runTest {
            // ----- Arrange -----
            coEvery {
                dao.getFetchedAtEpochMillis()
            } returns null

            // ----- Act -----
            val result = dataSource.getCachedAtEpochMillis()

            // ----- Assert -----
            result shouldBe null
        }
    }

    @Nested
    inner class CacheRoadmap {
        @Test
        fun `writes a RoadmapDocumentEntity built from the given markdown and timestamp`() = runTest {
            // ----- Arrange -----
            val markdown = "# Roadmap"
            val fetchedAtEpochMillis = 2_000L

            // ----- Act -----
            dataSource.cacheRoadmap(
                markdown = markdown,
                fetchedAtEpochMillis = fetchedAtEpochMillis,
            )

            // ----- Assert -----
            coVerify {
                dao.upsert(
                    entity = RoadmapDocumentEntity(
                        markdown = markdown,
                        fetchedAtEpochMillis = fetchedAtEpochMillis,
                    ),
                )
            }
        }
    }
}
