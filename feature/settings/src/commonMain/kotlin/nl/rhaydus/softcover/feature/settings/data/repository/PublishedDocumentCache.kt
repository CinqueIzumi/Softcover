package nl.rhaydus.softcover.feature.settings.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentBundledDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentKey
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentRemoteDataSource
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocument
import kotlin.time.Clock

// Published documents only change when the repo is edited - a live network hit on every screen visit
// would be needless chatter. Six hours balances "stale copy" risk against unnecessary requests;
// a forced refresh (pull-to-refresh, the error-slot retry) always bypasses it.
private const val CACHE_TTL_MILLIS = 6 * 60 * 60 * 1000L

/**
 * The cache-first policy shared by every published document: [observe] emits the cached copy whenever
 * one exists, falling back to the bundled build-time copy only when the cache is empty - first launch,
 * or offline before the first successful fetch. That fallback is a deliberate content decision, not a
 * swallowed error: both reads throw as usual, and the empty-cache case is a `null` row rather than a
 * failure.
 *
 * Which copy the reader is looking at travels on the document itself
 * ([PublishedDocument.source] / [PublishedDocument.fetchedAtEpochMillis]), assigned by whichever data
 * source produced it, so the screen can say so without asking the repository.
 */
internal class PublishedDocumentCache(
    private val remoteDataSource: PublishedDocumentRemoteDataSource,
    private val localDataSource: PublishedDocumentLocalDataSource,
    private val bundledDataSource: PublishedDocumentBundledDataSource,
) {
    fun observe(key: PublishedDocumentKey): Flow<PublishedDocument> =
        localDataSource.observeCachedDocument(key = key).map { cached ->
            cached ?: bundledDataSource.readBundledDocument(key = key)
        }

    suspend fun refresh(
        key: PublishedDocumentKey,
        force: Boolean,
    ) {
        if (force.not() && isFresh(key = key)) return

        val markdown = remoteDataSource.fetchMarkdown(key = key)

        localDataSource.cacheDocument(
            key = key,
            markdown = markdown,
            fetchedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
        )
    }

    private suspend fun isFresh(key: PublishedDocumentKey): Boolean {
        val fetchedAt = localDataSource.getCachedAtEpochMillis(key = key) ?: return false

        return Clock.System.now().toEpochMilliseconds() - fetchedAt < CACHE_TTL_MILLIS
    }
}
