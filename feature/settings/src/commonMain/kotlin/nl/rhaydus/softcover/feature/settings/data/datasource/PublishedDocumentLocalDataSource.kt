package nl.rhaydus.softcover.feature.settings.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.database.dao.PublishedDocumentDao
import nl.rhaydus.softcover.core.database.model.PublishedDocumentEntity
import nl.rhaydus.softcover.feature.settings.data.mapper.toPublishedDocument
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocument

internal interface PublishedDocumentLocalDataSource {
    /** The cached document for [key], or `null` until a fetch has landed. */
    fun observeCachedDocument(key: PublishedDocumentKey): Flow<PublishedDocument?>

    /** When the cached copy of [key] was fetched, or `null` when there is none. Drives the refresh TTL. */
    suspend fun getCachedAtEpochMillis(key: PublishedDocumentKey): Long?

    suspend fun cacheDocument(
        key: PublishedDocumentKey,
        markdown: String,
        fetchedAtEpochMillis: Long,
    )
}

internal class PublishedDocumentLocalDataSourceImpl(
    private val dao: PublishedDocumentDao,
) : PublishedDocumentLocalDataSource {
    override fun observeCachedDocument(key: PublishedDocumentKey): Flow<PublishedDocument?> =
        dao.observeDocument(key = key.storageKey).map { entity -> entity?.toPublishedDocument() }

    override suspend fun getCachedAtEpochMillis(key: PublishedDocumentKey): Long? =
        dao.getFetchedAtEpochMillis(key = key.storageKey)

    override suspend fun cacheDocument(
        key: PublishedDocumentKey,
        markdown: String,
        fetchedAtEpochMillis: Long,
    ) {
        dao.upsert(
            entity = PublishedDocumentEntity(
                key = key.storageKey,
                markdown = markdown,
                fetchedAtEpochMillis = fetchedAtEpochMillis,
            ),
        )
    }
}
