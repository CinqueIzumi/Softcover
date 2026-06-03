package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import nl.rhaydus.softcover.core.database.model.PendingUserBookWriteEntity

@Dao
interface PendingUserBookWriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplacing(entity: PendingUserBookWriteEntity)

    @Query("SELECT * FROM pending_user_book_writes WHERE attempts < :maxAttempts ORDER BY enqueuedAt ASC")
    suspend fun getPending(maxAttempts: Int = 5): List<PendingUserBookWriteEntity>

    @Query("DELETE FROM pending_user_book_writes WHERE localId = :localId")
    suspend fun delete(localId: Long)

    @Query("UPDATE pending_user_book_writes SET attempts = attempts + 1 WHERE localId = :localId")
    suspend fun incrementAttempts(localId: Long)
}
