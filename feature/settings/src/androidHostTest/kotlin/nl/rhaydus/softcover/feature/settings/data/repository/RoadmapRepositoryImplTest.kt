package nl.rhaydus.softcover.feature.settings.data.repository

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import nl.rhaydus.softcover.feature.settings.data.datasource.RoadmapBundledDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.RoadmapLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.RoadmapRemoteDataSource
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSource

private const val ONE_HOUR_MILLIS = 60 * 60 * 1000L

class RoadmapRepositoryImplTest {
    private lateinit var remoteDataSource: RoadmapRemoteDataSource
    private lateinit var localDataSource: RoadmapLocalDataSource
    private lateinit var bundledDataSource: RoadmapBundledDataSource
    private lateinit var repository: RoadmapRepositoryImpl

    @BeforeEach
    fun setUp() {
        remoteDataSource = mockk()
        localDataSource = mockk(relaxed = true)
        bundledDataSource = mockk()
        repository = RoadmapRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            bundledDataSource = bundledDataSource,
        )
    }

    private fun stubDocument(source: RoadmapSource): RoadmapDocument = RoadmapDocument(
        blocks = emptyList(),
        fetchedAtEpochMillis = 1_000L,
        source = source,
    )

    @Nested
    inner class ObserveRoadmap {
        @Test
        fun `emits the cached document when the local source has one`() = runTest {
            // ----- Arrange -----
            val cached = stubDocument(source = RoadmapSource.CACHE)

            every {
                localDataSource.observeCachedRoadmap()
            } returns flowOf(cached)

            // ----- Act & Assert -----
            repository.observeRoadmap().test {
                awaitItem() shouldBe cached
                awaitComplete()
            }
        }

        @Test
        fun `falls back to the bundled document when the local source emits null`() = runTest {
            // ----- Arrange -----
            val bundled = stubDocument(source = RoadmapSource.BUNDLED)

            every {
                localDataSource.observeCachedRoadmap()
            } returns flowOf(null)
            coEvery {
                bundledDataSource.readBundledRoadmap()
            } returns bundled

            // ----- Act & Assert -----
            repository.observeRoadmap().test {
                awaitItem() shouldBe bundled
                awaitComplete()
            }
        }

        @Test
        fun `replaces the bundled fallback with a later cache emission`() = runTest {
            // ----- Arrange -----
            val bundled = stubDocument(source = RoadmapSource.BUNDLED)
            val cached = stubDocument(source = RoadmapSource.CACHE)
            val cacheFlow = MutableSharedFlow<RoadmapDocument?>(replay = 1)
            cacheFlow.tryEmit(null)

            every {
                localDataSource.observeCachedRoadmap()
            } returns cacheFlow
            coEvery {
                bundledDataSource.readBundledRoadmap()
            } returns bundled

            // ----- Act & Assert -----
            repository.observeRoadmap().test {
                awaitItem() shouldBe bundled

                cacheFlow.emit(cached)

                awaitItem() shouldBe cached
            }
        }
    }

    @Nested
    inner class RefreshRoadmap {
        @Test
        fun `skips the network when the cached timestamp is younger than the TTL`() = runTest {
            // ----- Arrange -----
            val recentFetch = Clock.System.now().toEpochMilliseconds() - ONE_HOUR_MILLIS

            coEvery {
                localDataSource.getCachedAtEpochMillis()
            } returns recentFetch

            // ----- Act -----
            repository.refreshRoadmap(force = false)

            // ----- Assert -----
            coVerify(exactly = 0) {
                remoteDataSource.fetchRoadmapMarkdown()
            }
        }

        @Test
        fun `fetches when the cached timestamp is older than the TTL`() = runTest {
            // ----- Arrange -----
            val staleFetch = Clock.System.now().toEpochMilliseconds() - 7 * ONE_HOUR_MILLIS

            coEvery {
                localDataSource.getCachedAtEpochMillis()
            } returns staleFetch
            coEvery {
                remoteDataSource.fetchRoadmapMarkdown()
            } returns "# Roadmap"

            // ----- Act -----
            repository.refreshRoadmap(force = false)

            // ----- Assert -----
            coVerify {
                remoteDataSource.fetchRoadmapMarkdown()
            }
        }

        @Test
        fun `fetches when there is no cached copy at all`() = runTest {
            // ----- Arrange -----
            coEvery {
                localDataSource.getCachedAtEpochMillis()
            } returns null
            coEvery {
                remoteDataSource.fetchRoadmapMarkdown()
            } returns "# Roadmap"

            // ----- Act -----
            repository.refreshRoadmap(force = false)

            // ----- Assert -----
            coVerify {
                remoteDataSource.fetchRoadmapMarkdown()
            }
        }

        @Test
        fun `always fetches when forced even with a fresh cache`() = runTest {
            // ----- Arrange -----
            val recentFetch = Clock.System.now().toEpochMilliseconds() - ONE_HOUR_MILLIS

            coEvery {
                localDataSource.getCachedAtEpochMillis()
            } returns recentFetch
            coEvery {
                remoteDataSource.fetchRoadmapMarkdown()
            } returns "# Roadmap"

            // ----- Act -----
            repository.refreshRoadmap(force = true)

            // ----- Assert -----
            coVerify {
                remoteDataSource.fetchRoadmapMarkdown()
            }
        }

        @Test
        fun `stores the fetched markdown via cacheRoadmap on a successful fetch`() = runTest {
            // ----- Arrange -----
            val markdown = "# Roadmap"

            coEvery {
                localDataSource.getCachedAtEpochMillis()
            } returns null
            coEvery {
                remoteDataSource.fetchRoadmapMarkdown()
            } returns markdown

            // ----- Act -----
            repository.refreshRoadmap(force = false)

            // ----- Assert -----
            coVerify {
                localDataSource.cacheRoadmap(
                    markdown = markdown,
                    fetchedAtEpochMillis = any(),
                )
            }
        }

        @Test
        fun `propagates a throw from the remote source and leaves the cache untouched`() = runTest {
            // ----- Arrange -----
            val error = IllegalStateException("network down")

            coEvery {
                localDataSource.getCachedAtEpochMillis()
            } returns null
            coEvery {
                remoteDataSource.fetchRoadmapMarkdown()
            } throws error

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> {
                repository.refreshRoadmap(force = false)
            }

            coVerify(exactly = 0) {
                localDataSource.cacheRoadmap(
                    markdown = any(),
                    fetchedAtEpochMillis = any(),
                )
            }
        }
    }
}
