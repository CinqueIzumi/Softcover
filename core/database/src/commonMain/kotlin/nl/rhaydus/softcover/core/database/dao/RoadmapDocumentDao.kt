package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.RoadmapDocumentEntity

@Dao
interface RoadmapDocumentDao {
    @Upsert
    suspend fun upsert(entity: RoadmapDocumentEntity)

    @Query("SELECT * FROM roadmap_documents WHERE id = 0")
    fun observeRoadmapDocument(): Flow<RoadmapDocumentEntity?>

    // A projection of just the timestamp, for the cache-freshness check: it reads the one column that
    // decides whether to refresh instead of pulling the whole markdown body across for nothing. A
    // one-shot query rather than a terminal read of [observeRoadmapDocument], which would be the
    // crash-risk pattern the `UnguardedFlowTerminalRead` gate exists to catch.
    @Query("SELECT fetchedAtEpochMillis FROM roadmap_documents WHERE id = 0")
    suspend fun getFetchedAtEpochMillis(): Long?
}
