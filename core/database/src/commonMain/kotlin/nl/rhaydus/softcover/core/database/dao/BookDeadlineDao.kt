package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.BookDeadlineEntity

@Dao
interface BookDeadlineDao {
    @Upsert
    suspend fun upsert(entity: BookDeadlineEntity)

    @Query("DELETE FROM book_deadlines WHERE bookId = :bookId")
    suspend fun deleteByBookId(bookId: Int)

    @Query("SELECT * FROM book_deadlines WHERE bookId = :bookId LIMIT 1")
    fun observeByBookId(bookId: Int): Flow<BookDeadlineEntity?>

    @Query("SELECT * FROM book_deadlines")
    fun observeAll(): Flow<List<BookDeadlineEntity>>
}
