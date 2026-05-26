package nl.rhaydus.softcover.feature.connectivity.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import nl.rhaydus.softcover.feature.connectivity.data.model.PendingListWriteEntity

@Dao
interface PendingListWriteDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: PendingListWriteEntity)

    @Query("SELECT * FROM pending_list_writes WHERE attempts < :maxAttempts ORDER BY enqueuedAt ASC, localId ASC")
    suspend fun getPending(maxAttempts: Int = 5): List<PendingListWriteEntity>

    @Query("DELETE FROM pending_list_writes WHERE localId = :localId")
    suspend fun delete(localId: Long)

    @Query("UPDATE pending_list_writes SET attempts = attempts + 1 WHERE localId = :localId")
    suspend fun incrementAttempts(localId: Long)
}
