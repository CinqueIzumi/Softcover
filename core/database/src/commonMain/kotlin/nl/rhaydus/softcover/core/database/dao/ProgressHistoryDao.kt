package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.ProgressHistoryEntity
import nl.rhaydus.softcover.core.database.model.ProgressUpdateRow

@Dao
interface ProgressHistoryDao {
    @Query("SELECT * FROM progress_history WHERE bookId = :bookId ORDER BY actionAtEpochMillis, localId")
    fun observeByBookId(bookId: Int): Flow<List<ProgressHistoryEntity>>

    @Query("SELECT bookId, actionAtEpochMillis, pages, pagesRead FROM progress_history ORDER BY actionAtEpochMillis, localId")
    fun observeAllUpdates(): Flow<List<ProgressUpdateRow>>

    @Insert
    suspend fun insert(entity: ProgressHistoryEntity): Long

    @Insert
    suspend fun insertAll(entities: List<ProgressHistoryEntity>)

    @Query("UPDATE progress_history SET pending = 0 WHERE localId = :localId")
    suspend fun markSynced(localId: Long)

    @Query("DELETE FROM progress_history WHERE localId = :localId")
    suspend fun deleteById(localId: Long)

    @Query("DELETE FROM progress_history WHERE bookId = :bookId AND pending = 0")
    suspend fun deleteSyncedForBook(bookId: Int)

    @Query("DELETE FROM progress_history WHERE pending = 0")
    suspend fun deleteAllSynced()

    /**
     * Swaps the book's synced rows for [entities] in one transaction. Nothing is read before the fetch,
     * so overlapping refreshes each leave exactly the fetched rows rather than stacking duplicates.
     */
    @Transaction
    suspend fun replaceSyncedForBook(
        bookId: Int,
        entities: List<ProgressHistoryEntity>,
    ) {
        deleteSyncedForBook(bookId = bookId)
        insertAll(entities = entities)
    }

    /** [replaceSyncedForBook] across every book, so a book whose journals vanished remotely is cleared too. */
    @Transaction
    suspend fun replaceAllSynced(entities: List<ProgressHistoryEntity>) {
        deleteAllSynced()
        insertAll(entities = entities)
    }
}
