package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.ReadingSessionEntity

@Dao
interface ReadingSessionDao {
    @Insert
    suspend fun insert(entity: ReadingSessionEntity): Long

    @Update
    suspend fun update(entity: ReadingSessionEntity)

    @Query("DELETE FROM reading_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM reading_sessions WHERE bookId = :bookId ORDER BY startedAt DESC")
    fun observeByBookId(bookId: Int): Flow<List<ReadingSessionEntity>>

    @Query("SELECT * FROM reading_sessions ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<ReadingSessionEntity>>

    @Query("SELECT * FROM reading_sessions WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    fun observeActive(): Flow<ReadingSessionEntity?>

    @Query("SELECT * FROM reading_sessions WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ReadingSessionEntity?>
}
