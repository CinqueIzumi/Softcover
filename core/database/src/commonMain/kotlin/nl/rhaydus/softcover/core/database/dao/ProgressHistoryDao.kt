package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.ProgressHistoryEntity

@Dao
interface ProgressHistoryDao {
    @Query("SELECT * FROM progress_history WHERE bookId = :bookId ORDER BY actionAtEpochMillis, localId")
    fun observeByBookId(bookId: Int): Flow<List<ProgressHistoryEntity>>

    @Insert
    suspend fun insert(entity: ProgressHistoryEntity): Long

    @Insert
    suspend fun insertAll(entities: List<ProgressHistoryEntity>)

    @Query("UPDATE progress_history SET pending = 0 WHERE localId = :localId")
    suspend fun markSynced(localId: Long)

    @Query("DELETE FROM progress_history WHERE localId = :localId")
    suspend fun deleteById(localId: Long)

    @Query("SELECT localId FROM progress_history WHERE bookId = :bookId AND pending = 0")
    suspend fun getSyncedIdsForBook(bookId: Int): List<Long>

    @Query("DELETE FROM progress_history WHERE localId IN (:localIds)")
    suspend fun deleteByIds(localIds: List<Long>)

    @Transaction
    suspend fun replaceSynced(
        syncedIdsBeforeFetch: List<Long>,
        entities: List<ProgressHistoryEntity>,
    ) {
        deleteByIds(localIds = syncedIdsBeforeFetch)
        insertAll(entities = entities)
    }
}
