package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.HiddenRecommendationEntity

@Dao
interface HiddenRecommendationDao {
    @Query("SELECT * FROM hidden_recommendations")
    fun observeHiddenRecommendations(): Flow<List<HiddenRecommendationEntity>>

    @Upsert
    suspend fun hide(entity: HiddenRecommendationEntity)

    @Query("DELETE FROM hidden_recommendations WHERE bookId = :bookId")
    suspend fun restore(bookId: Int)
}
