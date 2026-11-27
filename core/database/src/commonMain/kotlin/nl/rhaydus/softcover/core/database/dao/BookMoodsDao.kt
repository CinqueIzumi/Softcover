package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.BookMoodEntity

@Dao
interface BookMoodsDao {
    @Query("SELECT * FROM book_moods")
    fun observeAll(): Flow<List<BookMoodEntity>>

    @Query("SELECT * FROM book_moods WHERE bookId = :bookId")
    fun observe(bookId: Int): Flow<List<BookMoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BookMoodEntity>)

    @Query("DELETE FROM book_moods WHERE bookId = :bookId")
    suspend fun deleteForBook(bookId: Int)

    @Transaction
    suspend fun replaceForBook(
        bookId: Int,
        entities: List<BookMoodEntity>,
    ) {
        deleteForBook(bookId = bookId)
        insertAll(entities = entities)
    }
}
