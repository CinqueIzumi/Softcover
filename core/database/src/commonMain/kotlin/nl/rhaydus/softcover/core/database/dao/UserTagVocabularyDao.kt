package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.UserTagVocabularyEntity

@Dao
interface UserTagVocabularyDao {
    @Query("SELECT * FROM user_tag_vocabulary WHERE userId = :userId")
    fun observe(userId: Int): Flow<List<UserTagVocabularyEntity>>

    @Upsert
    suspend fun upsertAll(entities: List<UserTagVocabularyEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entities: List<UserTagVocabularyEntity>)

    @Query("DELETE FROM user_tag_vocabulary WHERE userId = :userId")
    suspend fun clearForUser(userId: Int)

    @Transaction
    suspend fun replaceForUser(
        userId: Int,
        entities: List<UserTagVocabularyEntity>,
    ) {
        clearForUser(userId = userId)
        upsertAll(entities = entities)
    }
}
