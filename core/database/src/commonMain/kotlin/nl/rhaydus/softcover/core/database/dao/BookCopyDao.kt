package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.BookCopyEntity

@Dao
interface BookCopyDao {
    @Query("SELECT * FROM book_copies")
    fun observeAll(): Flow<List<BookCopyEntity>>

    @Query("SELECT * FROM book_copies WHERE bookId = :bookId")
    fun observe(bookId: Int): Flow<BookCopyEntity?>

    @Query("SELECT * FROM book_copies WHERE bookId = :bookId")
    suspend fun get(bookId: Int): BookCopyEntity?

    @Upsert
    suspend fun upsert(entity: BookCopyEntity)

    @Query("DELETE FROM book_copies WHERE bookId = :bookId")
    suspend fun delete(bookId: Int)
}
