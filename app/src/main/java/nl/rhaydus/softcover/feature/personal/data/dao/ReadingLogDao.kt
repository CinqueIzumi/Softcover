package nl.rhaydus.softcover.feature.personal.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.personal.data.model.ReadingLogEntryEntity

@Dao
interface ReadingLogDao {
    @Insert
    suspend fun insert(entity: ReadingLogEntryEntity): Long

    @Update
    suspend fun update(entity: ReadingLogEntryEntity)

    @Query("DELETE FROM reading_log_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM reading_log_entries WHERE bookId = :bookId ORDER BY COALESCE(finishedAt, startedAt, createdAt) DESC")
    fun observeByBookId(bookId: Int): Flow<List<ReadingLogEntryEntity>>

    @Query("SELECT COUNT(*) FROM reading_log_entries WHERE bookId = :bookId")
    fun observeCountByBookId(bookId: Int): Flow<Int>
}
