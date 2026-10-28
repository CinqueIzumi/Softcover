package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.BookTraitsEntity

@Dao
interface BookTraitsDao {
    @Query("SELECT * FROM book_traits WHERE bookId = :bookId")
    fun observe(bookId: Int): Flow<BookTraitsEntity?>

    @Query("SELECT * FROM book_traits WHERE bookId = :bookId")
    suspend fun get(bookId: Int): BookTraitsEntity?

    @Upsert
    suspend fun upsert(entity: BookTraitsEntity)

    @Query("DELETE FROM book_traits WHERE bookId = :bookId")
    suspend fun delete(bookId: Int)
}
