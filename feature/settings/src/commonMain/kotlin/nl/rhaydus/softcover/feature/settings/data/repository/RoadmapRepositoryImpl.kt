package nl.rhaydus.softcover.feature.settings.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import nl.rhaydus.softcover.feature.settings.data.datasource.RoadmapBundledDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.RoadmapLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.RoadmapRemoteDataSource
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument
import nl.rhaydus.softcover.feature.settings.domain.repository.RoadmapRepository

// The roadmap only changes when a milestone is edited or closed - a live network hit on every screen
// visit would be needless chatter. Six hours balances "stale copy" risk against unnecessary requests;
// a forced refresh (pull-to-refresh, the error-slot retry) always bypasses it.
private const val ROADMAP_CACHE_TTL_MILLIS = 6 * 60 * 60 * 1000L

/**
 * Cache-first: [observeRoadmap] emits the cached copy whenever one exists, falling back to the bundled
 * build-time copy only when the cache is empty - first launch, or offline before the first successful
 * fetch. That fallback is a deliberate content decision, not a swallowed error: both reads throw as
 * usual, and the empty-cache case is a `null` row rather than a failure.
 *
 * Which copy the reader is looking at travels on the document itself
 * ([RoadmapDocument.source] / [RoadmapDocument.fetchedAtEpochMillis]), assigned by whichever data
 * source produced it, so the screen can say so without asking the repository.
 */
internal class RoadmapRepositoryImpl(
    private val remoteDataSource: RoadmapRemoteDataSource,
    private val localDataSource: RoadmapLocalDataSource,
    private val bundledDataSource: RoadmapBundledDataSource,
) : RoadmapRepository {
    override fun observeRoadmap(): Flow<RoadmapDocument> = localDataSource.observeCachedRoadmap().map { cached ->
        cached ?: bundledDataSource.readBundledRoadmap()
    }

    override suspend fun refreshRoadmap(force: Boolean) {
        if (force.not() && cacheIsFresh()) return

        val markdown = remoteDataSource.fetchRoadmapMarkdown()

        localDataSource.cacheRoadmap(
            markdown = markdown,
            fetchedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
        )
    }

    private suspend fun cacheIsFresh(): Boolean {
        val fetchedAt = localDataSource.getCachedAtEpochMillis() ?: return false

        return Clock.System.now().toEpochMilliseconds() - fetchedAt < ROADMAP_CACHE_TTL_MILLIS
    }
}
