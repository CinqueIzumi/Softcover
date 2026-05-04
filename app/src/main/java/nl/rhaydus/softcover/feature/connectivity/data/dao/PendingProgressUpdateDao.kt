package nl.rhaydus.softcover.feature.connectivity.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import nl.rhaydus.softcover.feature.connectivity.data.model.PendingProgressUpdateEntity

@Dao
interface PendingProgressUpdateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplacing(entity: PendingProgressUpdateEntity)

    @Query("SELECT * FROM pending_progress_updates WHERE attempts < :maxAttempts ORDER BY enqueuedAt ASC")
    suspend fun getPending(maxAttempts: Int = 5): List<PendingProgressUpdateEntity>

    @Query("DELETE FROM pending_progress_updates WHERE localId = :localId")
    suspend fun delete(localId: Long)

    @Query("UPDATE pending_progress_updates SET attempts = attempts + 1 WHERE localId = :localId")
    suspend fun incrementAttempts(localId: Long)
}
