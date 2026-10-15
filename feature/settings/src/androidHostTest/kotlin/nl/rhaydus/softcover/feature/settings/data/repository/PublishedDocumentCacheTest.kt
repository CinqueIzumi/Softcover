package nl.rhaydus.softcover.feature.settings.data.repository

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentBundledDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentKey
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentRemoteDataSource
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocument
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocumentSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock

private const val ONE_HOUR_MILLIS = 60 * 60 * 1000L

class PublishedDocumentCacheTest {
    private lateinit var remoteDataSource: PublishedDocumentRemoteDataSource
    private lateinit var localDataSource: PublishedDocumentLocalDataSource
    private lateinit var bundledDataSource: PublishedDocumentBundledDataSource
    private lateinit var cache: PublishedDocumentCache

    @BeforeEach
    fun setUp() {
        remoteDataSource = mockk()
        localDataSource = mockk(relaxed = true)
        bundledDataSource = mockk()
        cache = PublishedDocumentCache(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            bundledDataSource = bundledDataSource,
        )
    }

    private fun stubDocument(source: PublishedDocumentSource): PublishedDocument = PublishedDocument(
        blocks = persistentListOf(),
        fetchedAtEpochMillis = 1_000L,
        source = source,
    )

    @Nested
    inner class Observe {
        @Test
        fun `emits the cached document when the local source has one`() = runTest {
            // ----- Arrange -----
            val cached = stubDocument(source = PublishedDocumentSource.CACHE)

            every {
                localDataSource.observeCachedDocument(key = PublishedDocumentKey.ROADMAP)
            } returns flowOf(cached)

            // ----- Act & Assert -----
            cache.observe(key = PublishedDocumentKey.ROADMAP).test {
                awaitItem() shouldBe cached
                awaitComplete()
            }
        }

        @Test
        fun `falls back to the bundled document when the local source emits null`() = runTest {
            // ----- Arrange -----
            val bundled = stubDocument(source = PublishedDocumentSource.BUNDLED)

            every {
                localDataSource.observeCachedDocument(key = PublishedDocumentKey.ROADMAP)
            } returns flowOf(null)
            coEvery {
                bundledDataSource.readBundledDocument(key = PublishedDocumentKey.ROADMAP)
            } returns bundled

            // ----- Act & Assert -----
            cache.observe(key = PublishedDocumentKey.ROADMAP).test {
                awaitItem() shouldBe bundled
                awaitComplete()
            }
        }

        @Test
        fun `replaces the bundled fallback with a later cache emission`() = runTest {
            // ----- Arrange -----
            val bundled = stubDocument(source = PublishedDocumentSource.BUNDLED)
            val cached = stubDocument(source = PublishedDocumentSource.CACHE)
            val cacheFlow = MutableSharedFlow<PublishedDocument?>(replay = 1)
            cacheFlow.tryEmit(null)

            every {
                localDataSource.observeCachedDocument(key = PublishedDocumentKey.ROADMAP)
            } returns cacheFlow
            coEvery {
                bundledDataSource.readBundledDocument(key = PublishedDocumentKey.ROADMAP)
            } returns bundled

            // ----- Act & Assert -----
            cache.observe(key = PublishedDocumentKey.ROADMAP).test {
                awaitItem() shouldBe bundled

                cacheFlow.emit(cached)

                awaitItem() shouldBe cached
            }
        }
    }

    @Nested
    inner class Refresh {
        @Test
        fun `skips the network when the cached timestamp is younger than the TTL`() = runTest {
            // ----- Arrange -----
            val recentFetch = Clock.System.now().toEpochMilliseconds() - ONE_HOUR_MILLIS

            coEvery {
                localDataSource.getCachedAtEpochMillis(key = PublishedDocumentKey.ROADMAP)
            } returns recentFetch

            // ----- Act -----
            cache.refresh(
                key = PublishedDocumentKey.ROADMAP,
                force = false,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                remoteDataSource.fetchMarkdown(key = PublishedDocumentKey.ROADMAP)
            }
        }

        @Test
        fun `fetches when the cached timestamp is older than the TTL`() = runTest {
            // ----- Arrange -----
            val staleFetch = Clock.System.now().toEpochMilliseconds() - 7 * ONE_HOUR_MILLIS

            coEvery {
                localDataSource.getCachedAtEpochMillis(key = PublishedDocumentKey.ROADMAP)
            } returns staleFetch
            coEvery {
                remoteDataSource.fetchMarkdown(key = PublishedDocumentKey.ROADMAP)
            } returns "# Roadmap"

            // ----- Act -----
            cache.refresh(
                key = PublishedDocumentKey.ROADMAP,
                force = false,
            )

            // ----- Assert -----
            coVerify {
                remoteDataSource.fetchMarkdown(key = PublishedDocumentKey.ROADMAP)
            }
        }

        @Test
        fun `fetches when there is no cached copy at all`() = runTest {
            // ----- Arrange -----
            coEvery {
                localDataSource.getCachedAtEpochMillis(key = PublishedDocumentKey.ROADMAP)
            } returns null
            coEvery {
                remoteDataSource.fetchMarkdown(key = PublishedDocumentKey.ROADMAP)
            } returns "# Roadmap"

            // ----- Act -----
            cache.refresh(
                key = PublishedDocumentKey.ROADMAP,
                force = false,
            )

            // ----- Assert -----
            coVerify {
                remoteDataSource.fetchMarkdown(key = PublishedDocumentKey.ROADMAP)
            }
        }

        @Test
        fun `always fetches when forced even with a fresh cache`() = runTest {
            // ----- Arrange -----
            val recentFetch = Clock.System.now().toEpochMilliseconds() - ONE_HOUR_MILLIS

            coEvery {
                localDataSource.getCachedAtEpochMillis(key = PublishedDocumentKey.ROADMAP)
            } returns recentFetch
            coEvery {
                remoteDataSource.fetchMarkdown(key = PublishedDocumentKey.ROADMAP)
            } returns "# Roadmap"

            // ----- Act -----
            cache.refresh(
                key = PublishedDocumentKey.ROADMAP,
                force = true,
            )

            // ----- Assert -----
            coVerify {
                remoteDataSource.fetchMarkdown(key = PublishedDocumentKey.ROADMAP)
            }
        }

        @Test
        fun `stores the fetched markdown via cacheDocument on a successful fetch`() = runTest {
            // ----- Arrange -----
            val markdown = "# Roadmap"

            coEvery {
                localDataSource.getCachedAtEpochMillis(key = PublishedDocumentKey.ROADMAP)
            } returns null
            coEvery {
                remoteDataSource.fetchMarkdown(key = PublishedDocumentKey.ROADMAP)
            } returns markdown

            // ----- Act -----
            cache.refresh(
                key = PublishedDocumentKey.ROADMAP,
                force = false,
            )

            // ----- Assert -----
            coVerify {
                localDataSource.cacheDocument(
                    key = PublishedDocumentKey.ROADMAP,
                    markdown = markdown,
                    fetchedAtEpochMillis = any(),
                )
            }
        }

        @Test
        fun `stamps the cached copy with the time of the refresh`() = runTest {
            // ----- Arrange -----
            val before = Clock.System.now().toEpochMilliseconds()
            val stamp = slot<Long>()

            coEvery {
                localDataSource.getCachedAtEpochMillis(key = PublishedDocumentKey.ROADMAP)
            } returns null
            coEvery {
                remoteDataSource.fetchMarkdown(key = PublishedDocumentKey.ROADMAP)
            } returns "# Roadmap"

            // ----- Act -----
            cache.refresh(
                key = PublishedDocumentKey.ROADMAP,
                force = false,
            )

            // ----- Assert -----
            coVerify {
                localDataSource.cacheDocument(
                    key = PublishedDocumentKey.ROADMAP,
                    markdown = any(),
                    fetchedAtEpochMillis = capture(stamp),
                )
            }
            val after = Clock.System.now().toEpochMilliseconds()
            (stamp.captured in before..after) shouldBe true
        }

        @Test
        fun `propagates a throw from the remote source and leaves the cache untouched`() = runTest {
            // ----- Arrange -----
            val error = IllegalStateException("network down")

            coEvery {
                localDataSource.getCachedAtEpochMillis(key = PublishedDocumentKey.ROADMAP)
            } returns null
            coEvery {
                remoteDataSource.fetchMarkdown(key = PublishedDocumentKey.ROADMAP)
            } throws error

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> {
                cache.refresh(
                key = PublishedDocumentKey.ROADMAP,
                force = false,
            )
            }

            coVerify(exactly = 0) {
                localDataSource.cacheDocument(
                    key = PublishedDocumentKey.ROADMAP,
                    markdown = any(),
                    fetchedAtEpochMillis = any(),
                )
            }
        }
    }

    @Nested
    inner class KeyIsolation {
        @Test
        fun `observes the cached and bundled copies of the requested key only`() = runTest {
            // ----- Arrange -----
            val bundled = stubDocument(source = PublishedDocumentSource.BUNDLED)

            every {
                localDataSource.observeCachedDocument(key = PublishedDocumentKey.CHANGELOG)
            } returns flowOf(null)
            coEvery {
                bundledDataSource.readBundledDocument(key = PublishedDocumentKey.CHANGELOG)
            } returns bundled

            // ----- Act & Assert -----
            cache.observe(key = PublishedDocumentKey.CHANGELOG).test {
                awaitItem() shouldBe bundled
                awaitComplete()
            }

            coVerify(exactly = 0) {
                bundledDataSource.readBundledDocument(key = PublishedDocumentKey.ROADMAP)
            }
        }

        @Test
        fun `judges freshness per key so a fresh roadmap does not suppress a changelog fetch`() = runTest {
            // ----- Arrange -----
            val now = Clock.System.now().toEpochMilliseconds()

            coEvery {
                localDataSource.getCachedAtEpochMillis(key = PublishedDocumentKey.ROADMAP)
            } returns now - ONE_HOUR_MILLIS
            coEvery {
                localDataSource.getCachedAtEpochMillis(key = PublishedDocumentKey.CHANGELOG)
            } returns now - 7 * ONE_HOUR_MILLIS
            coEvery {
                remoteDataSource.fetchMarkdown(key = PublishedDocumentKey.CHANGELOG)
            } returns "# Changelog"

            // ----- Act -----
            cache.refresh(
                key = PublishedDocumentKey.CHANGELOG,
                force = false,
            )

            // ----- Assert -----
            coVerify {
                localDataSource.cacheDocument(
                    key = PublishedDocumentKey.CHANGELOG,
                    markdown = "# Changelog",
                    fetchedAtEpochMillis = any(),
                )
            }
            coVerify(exactly = 0) {
                remoteDataSource.fetchMarkdown(key = PublishedDocumentKey.ROADMAP)
            }
        }

        @Test
        fun `skips a fresh changelog even when the roadmap is stale`() = runTest {
            // ----- Arrange -----
            val now = Clock.System.now().toEpochMilliseconds()

            coEvery {
                localDataSource.getCachedAtEpochMillis(key = PublishedDocumentKey.CHANGELOG)
            } returns now - ONE_HOUR_MILLIS
            coEvery {
                localDataSource.getCachedAtEpochMillis(key = PublishedDocumentKey.ROADMAP)
            } returns null

            // ----- Act -----
            cache.refresh(
                key = PublishedDocumentKey.CHANGELOG,
                force = false,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                remoteDataSource.fetchMarkdown(key = any())
            }
        }

        @Test
        fun `a forced refresh fetches and stores under the requested key`() = runTest {
            // ----- Arrange -----
            coEvery {
                remoteDataSource.fetchMarkdown(key = PublishedDocumentKey.CHANGELOG)
            } returns "# Changelog"

            // ----- Act -----
            cache.refresh(
                key = PublishedDocumentKey.CHANGELOG,
                force = true,
            )

            // ----- Assert -----
            coVerify {
                localDataSource.cacheDocument(
                    key = PublishedDocumentKey.CHANGELOG,
                    markdown = "# Changelog",
                    fetchedAtEpochMillis = any(),
                )
            }
            coVerify(exactly = 0) {
                localDataSource.cacheDocument(
                    key = PublishedDocumentKey.ROADMAP,
                    markdown = any(),
                    fetchedAtEpochMillis = any(),
                )
            }
        }
    }
}
