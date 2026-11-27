package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.DidNotFinishReasonsEntity

@Dao
interface DidNotFinishReasonsDao {
    @Query("SELECT * FROM did_not_finish_reasons")
    fun observeAll(): Flow<List<DidNotFinishReasonsEntity>>

    @Query("SELECT * FROM did_not_finish_reasons WHERE bookId = :bookId")
    fun observe(bookId: Int): Flow<DidNotFinishReasonsEntity?>

    @Query("SELECT * FROM did_not_finish_reasons WHERE bookId = :bookId")
    suspend fun get(bookId: Int): DidNotFinishReasonsEntity?

    @Upsert
    suspend fun upsert(entity: DidNotFinishReasonsEntity)

    @Query("DELETE FROM did_not_finish_reasons WHERE bookId = :bookId")
    suspend fun delete(bookId: Int)
}
