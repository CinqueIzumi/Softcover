package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.PublishedDocumentEntity

@Dao
interface PublishedDocumentDao {
    @Upsert
    suspend fun upsert(entity: PublishedDocumentEntity)

    @Query("SELECT * FROM published_documents WHERE `key` = :key")
    fun observeDocument(key: String): Flow<PublishedDocumentEntity?>

    // A projection of just the timestamp, for the cache-freshness check: it reads the one column that
    // decides whether to refresh instead of pulling the whole markdown body across for nothing. A
    // one-shot query rather than a terminal read of [observeDocument], which would be the
    // crash-risk pattern the `UnguardedFlowTerminalRead` gate exists to catch.
    @Query("SELECT fetchedAtEpochMillis FROM published_documents WHERE `key` = :key")
    suspend fun getFetchedAtEpochMillis(key: String): Long?
}
