package nl.rhaydus.softcover.feature.personal.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.personal.data.model.HighlightEntity

@Dao
interface HighlightDao {
    @Insert
    suspend fun insert(entity: HighlightEntity): Long

    @Update
    suspend fun update(entity: HighlightEntity)

    @Query("DELETE FROM book_highlights WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM book_highlights WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun observeByBookId(bookId: Int): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM book_highlights ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM book_highlights WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<HighlightEntity?>
}
