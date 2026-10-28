package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.BookUserTagEntity

@Dao
interface BookUserTagDao {
    @Query("SELECT * FROM book_user_tags")
    fun observeAll(): Flow<List<BookUserTagEntity>>

    @Query("SELECT * FROM book_user_tags WHERE bookId = :bookId")
    fun observeForBook(bookId: Int): Flow<List<BookUserTagEntity>>

    @Query("SELECT * FROM book_user_tags WHERE bookId = :bookId")
    suspend fun getForBook(bookId: Int): List<BookUserTagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BookUserTagEntity>)

    @Query("DELETE FROM book_user_tags WHERE bookId IN (:bookIds)")
    suspend fun clearForBooks(bookIds: Collection<Int>)

    @Transaction
    suspend fun replaceForBooks(
        bookIds: Collection<Int>,
        rows: List<BookUserTagEntity>,
    ) {
        clearForBooks(bookIds = bookIds)
        insertAll(entities = rows)
    }

    @Transaction
    suspend fun replaceForBook(
        bookId: Int,
        rows: List<BookUserTagEntity>,
    ) {
        replaceForBooks(
            bookIds = listOf(bookId),
            rows = rows,
        )
    }
}
