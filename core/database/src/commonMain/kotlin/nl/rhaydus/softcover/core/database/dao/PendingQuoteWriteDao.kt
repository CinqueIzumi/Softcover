package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import nl.rhaydus.softcover.core.database.model.PendingQuoteWriteEntity

@Dao
interface PendingQuoteWriteDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: PendingQuoteWriteEntity)

    @Query("SELECT * FROM pending_quote_writes ORDER BY enqueuedAt ASC, localId ASC")
    suspend fun getPending(): List<PendingQuoteWriteEntity>

    @Query("DELETE FROM pending_quote_writes WHERE localId = :localId")
    suspend fun delete(localId: Long)

    @Query("UPDATE pending_quote_writes SET attempts = attempts + 1 WHERE localId = :localId")
    suspend fun incrementAttempts(localId: Long)
}
