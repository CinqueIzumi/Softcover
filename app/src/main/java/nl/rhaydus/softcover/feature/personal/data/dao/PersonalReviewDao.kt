package nl.rhaydus.softcover.feature.personal.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.personal.data.model.PersonalReviewEntity

@Dao
interface PersonalReviewDao {
    @Upsert
    suspend fun upsert(entity: PersonalReviewEntity)

    @Query("DELETE FROM personal_reviews WHERE bookId = :bookId")
    suspend fun deleteByBookId(bookId: Int)

    @Query("SELECT * FROM personal_reviews WHERE bookId = :bookId LIMIT 1")
    fun observeByBookId(bookId: Int): Flow<PersonalReviewEntity?>

    @Query("SELECT * FROM personal_reviews")
    fun observeAll(): Flow<List<PersonalReviewEntity>>
}
