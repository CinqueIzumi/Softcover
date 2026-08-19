package nl.rhaydus.softcover.feature.settings.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.database.dao.RoadmapDocumentDao
import nl.rhaydus.softcover.core.database.model.RoadmapDocumentEntity
import nl.rhaydus.softcover.feature.settings.data.mapper.toRoadmapDocument
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument

interface RoadmapLocalDataSource {
    /** The cached roadmap, or `null` until a fetch has landed. */
    fun observeCachedRoadmap(): Flow<RoadmapDocument?>

    /** When the cached copy was fetched, or `null` when there is none. Drives the refresh TTL. */
    suspend fun getCachedAtEpochMillis(): Long?

    suspend fun cacheRoadmap(
        markdown: String,
        fetchedAtEpochMillis: Long,
    )
}

internal class RoadmapLocalDataSourceImpl(
    private val dao: RoadmapDocumentDao,
) : RoadmapLocalDataSource {
    override fun observeCachedRoadmap(): Flow<RoadmapDocument?> =
        dao.observeRoadmapDocument().map { entity -> entity?.toRoadmapDocument() }

    override suspend fun getCachedAtEpochMillis(): Long? = dao.getFetchedAtEpochMillis()

    override suspend fun cacheRoadmap(
        markdown: String,
        fetchedAtEpochMillis: Long,
    ) {
        dao.upsert(
            entity = RoadmapDocumentEntity(
                markdown = markdown,
                fetchedAtEpochMillis = fetchedAtEpochMillis,
            ),
        )
    }
}
