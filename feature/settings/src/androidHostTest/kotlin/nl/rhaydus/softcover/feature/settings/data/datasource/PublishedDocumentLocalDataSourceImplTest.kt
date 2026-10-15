package nl.rhaydus.softcover.feature.settings.data.datasource

import app.cash.turbine.test
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.dao.PublishedDocumentDao
import nl.rhaydus.softcover.core.database.model.PublishedDocumentEntity
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocumentSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PublishedDocumentLocalDataSourceImplTest {
    private lateinit var dao: PublishedDocumentDao
    private lateinit var dataSource: PublishedDocumentLocalDataSourceImpl

    @BeforeEach
    fun setUp() {
        dao = mockk(relaxed = true)
        dataSource = PublishedDocumentLocalDataSourceImpl(dao = dao)
    }

    private fun stubEntity(
        key: String = PublishedDocumentKey.ROADMAP.storageKey,
        markdown: String = "# Roadmap",
    ): PublishedDocumentEntity = PublishedDocumentEntity(
        key = key,
        markdown = markdown,
        fetchedAtEpochMillis = 1_000L,
    )

    @Nested
    inner class ObserveCachedDocument {
        @Test
        fun `maps the dao entity to a PublishedDocument sourced from CACHE`() = runTest {
            // ----- Arrange -----
            val entity = stubEntity()

            every {
                dao.observeDocument(key = PublishedDocumentKey.ROADMAP.storageKey)
            } returns flowOf(entity)

            // ----- Act & Assert -----
            dataSource.observeCachedDocument(key = PublishedDocumentKey.ROADMAP).test {
                val document = awaitItem().shouldNotBeNull()
                document.source shouldBe PublishedDocumentSource.CACHE
                document.fetchedAtEpochMillis shouldBe entity.fetchedAtEpochMillis
                awaitComplete()
            }
        }

        @Test
        fun `passes through null when the dao has no cached row`() = runTest {
            // ----- Arrange -----
            every {
                dao.observeDocument(key = PublishedDocumentKey.ROADMAP.storageKey)
            } returns flowOf(null)

            // ----- Act & Assert -----
            dataSource.observeCachedDocument(key = PublishedDocumentKey.ROADMAP).test {
                awaitItem() shouldBe null
                awaitComplete()
            }
        }

        @Test
        fun `observes the row of the requested key only`() = runTest {
            // ----- Arrange -----
            every {
                dao.observeDocument(key = PublishedDocumentKey.ROADMAP.storageKey)
            } returns flowOf(stubEntity(markdown = "roadmap row"))
            every {
                dao.observeDocument(key = PublishedDocumentKey.CHANGELOG.storageKey)
            } returns flowOf(null)

            // ----- Act & Assert -----
            dataSource.observeCachedDocument(key = PublishedDocumentKey.CHANGELOG).test {
                awaitItem() shouldBe null
                awaitComplete()
            }

            verify(exactly = 0) {
                dao.observeDocument(key = PublishedDocumentKey.ROADMAP.storageKey)
            }
        }
    }

    @Nested
    inner class GetCachedAtEpochMillis {
        @Test
        fun `reads the fetch time stored under the key's storage string`() = runTest {
            // ----- Arrange -----
            coEvery {
                dao.getFetchedAtEpochMillis(key = PublishedDocumentKey.ROADMAP.storageKey)
            } returns 111L
            coEvery {
                dao.getFetchedAtEpochMillis(key = PublishedDocumentKey.CHANGELOG.storageKey)
            } returns 222L

            // ----- Act -----
            val roadmapAt = dataSource.getCachedAtEpochMillis(key = PublishedDocumentKey.ROADMAP)
            val changelogAt = dataSource.getCachedAtEpochMillis(key = PublishedDocumentKey.CHANGELOG)

            // ----- Assert -----
            roadmapAt shouldBe 111L
            changelogAt shouldBe 222L
        }

        @Test
        fun `returns null when no row exists for the key`() = runTest {
            // ----- Arrange -----
            coEvery {
                dao.getFetchedAtEpochMillis(key = PublishedDocumentKey.CHANGELOG.storageKey)
            } returns null

            // ----- Act -----
            val cachedAt = dataSource.getCachedAtEpochMillis(key = PublishedDocumentKey.CHANGELOG)

            // ----- Assert -----
            cachedAt shouldBe null
        }
    }

    @Nested
    inner class CacheDocument {
        @Test
        fun `writes an entity built from the given markdown and timestamp under the key`() = runTest {
            // ----- Arrange -----
            val markdown = "# Roadmap"
            val fetchedAtEpochMillis = 2_000L

            // ----- Act -----
            dataSource.cacheDocument(
                key = PublishedDocumentKey.ROADMAP,
                markdown = markdown,
                fetchedAtEpochMillis = fetchedAtEpochMillis,
            )

            // ----- Assert -----
            coVerify {
                dao.upsert(
                    entity = PublishedDocumentEntity(
                        key = PublishedDocumentKey.ROADMAP.storageKey,
                        markdown = markdown,
                        fetchedAtEpochMillis = fetchedAtEpochMillis,
                    ),
                )
            }
        }

        @Test
        fun `roadmap and changelog writes land in separate rows`() = runTest {
            // ----- Arrange -----
            val markdown = "same body"

            // ----- Act -----
            dataSource.cacheDocument(
                key = PublishedDocumentKey.ROADMAP,
                markdown = markdown,
                fetchedAtEpochMillis = 1L,
            )
            dataSource.cacheDocument(
                key = PublishedDocumentKey.CHANGELOG,
                markdown = markdown,
                fetchedAtEpochMillis = 1L,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                dao.upsert(
                    entity = PublishedDocumentEntity(
                        key = PublishedDocumentKey.ROADMAP.storageKey,
                        markdown = markdown,
                        fetchedAtEpochMillis = 1L,
                    ),
                )
            }
            coVerify(exactly = 1) {
                dao.upsert(
                    entity = PublishedDocumentEntity(
                        key = PublishedDocumentKey.CHANGELOG.storageKey,
                        markdown = markdown,
                        fetchedAtEpochMillis = 1L,
                    ),
                )
            }
        }
    }
}
