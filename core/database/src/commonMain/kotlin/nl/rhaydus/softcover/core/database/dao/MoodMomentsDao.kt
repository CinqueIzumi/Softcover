package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.MoodMomentEntity

@Dao
interface MoodMomentsDao {
    @Query("SELECT * FROM mood_moments")
    fun observeAll(): Flow<List<MoodMomentEntity>>

    @Query("SELECT * FROM mood_moments WHERE bookId = :bookId")
    fun observe(bookId: Int): Flow<List<MoodMomentEntity>>

    @Insert
    suspend fun insert(entity: MoodMomentEntity): Long

    @Update
    suspend fun update(entity: MoodMomentEntity)

    @Query("DELETE FROM mood_moments WHERE id = :id")
    suspend fun delete(id: Long)
}
