package nl.rhaydus.softcover.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.model.PendingQuoteWriteEntity
import nl.rhaydus.softcover.core.database.model.QuoteEntity
import nl.rhaydus.softcover.core.database.model.QuoteSyncState

private const val KIND_INSERT = "INSERT"
private const val KIND_DELETE = "DELETE"

@Dao
interface QuoteDao {
    @Query(
        """
        SELECT * FROM book_quotes
        WHERE bookId = :bookId AND syncState != 'PENDING_DELETE'
        ORDER BY actionAtEpochMillis DESC, localId DESC
        """,
    )
    fun observeForBook(bookId: Int): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM book_quotes WHERE localId = :localId")
    suspend fun getById(localId: Long): QuoteEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertQuote(entity: QuoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSynced(entity: QuoteEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPendingWrite(entity: PendingQuoteWriteEntity)

    @Query("UPDATE book_quotes SET syncState = :state WHERE localId = :localId")
    suspend fun setSyncState(
        localId: Long,
        state: QuoteSyncState,
    )

    @Query("DELETE FROM book_quotes WHERE localId = :localId")
    suspend fun deleteById(localId: Long)

    @Query("DELETE FROM pending_quote_writes WHERE quoteLocalId = :quoteLocalId")
    suspend fun deletePendingWritesFor(quoteLocalId: Long)

    @Query("DELETE FROM pending_quote_writes WHERE quoteLocalId = :quoteLocalId AND kind = 'INSERT'")
    suspend fun deletePendingInsertFor(quoteLocalId: Long)

    @Query("DELETE FROM book_quotes WHERE bookId = :bookId AND syncState = 'SYNCED'")
    suspend fun deleteSyncedForBook(bookId: Int)

    @Query("DELETE FROM book_quotes WHERE remoteId = :remoteId AND localId != :exceptLocalId AND syncState = 'SYNCED'")
    suspend fun deleteSyncedWithRemoteId(
        remoteId: Long,
        exceptLocalId: Long,
    )

    @Query("SELECT COUNT(*) > 0 FROM book_quotes WHERE remoteId = :remoteId AND syncState != 'SYNCED'")
    suspend fun hasUnsyncedRowWithRemoteId(remoteId: Long): Boolean

    @Query("SELECT * FROM book_quotes WHERE bookId = :bookId AND syncState = 'PENDING_INSERT'")
    suspend fun getPendingInsertsForBook(bookId: Int): List<QuoteEntity>

    @Query("UPDATE book_quotes SET remoteId = :remoteId, syncState = 'SYNCED' WHERE localId = :localId")
    suspend fun setSynced(
        localId: Long,
        remoteId: Long,
    ): Int

    @Transaction
    suspend fun insertWithWrite(
        entity: QuoteEntity,
        enqueuedAt: String,
    ): Long {
        val localId: Long = insertQuote(entity = entity)

        insertPendingWrite(
            entity = PendingQuoteWriteEntity(
                kind = KIND_INSERT,
                quoteLocalId = localId,
                enqueuedAt = enqueuedAt,
            ),
        )

        return localId
    }

    /**
     * Removes a quote locally. A row Hardcover never saw is dropped with its queued writes; a synced row
     * is hidden as [QuoteSyncState.PENDING_DELETE] and a DELETE is queued. Returns whether a remote delete
     * was queued, so the caller knows to drain.
     */
    @Transaction
    suspend fun removeQuote(
        localId: Long,
        enqueuedAt: String,
    ): Boolean {
        val entity: QuoteEntity = getById(localId = localId) ?: return false

        if (entity.remoteId == null) {
            deletePendingWritesFor(quoteLocalId = localId)
            deleteById(localId = localId)

            return false
        }

        if (entity.syncState == QuoteSyncState.PENDING_DELETE) return false

        setSyncState(
            localId = localId,
            state = QuoteSyncState.PENDING_DELETE,
        )
        insertPendingWrite(
            entity = PendingQuoteWriteEntity(
                kind = KIND_DELETE,
                quoteLocalId = localId,
                enqueuedAt = enqueuedAt,
            ),
        )

        return true
    }

    /**
     * Removes any synced copy of [remoteId] first: a refresh may have pulled the server's copy of this
     * quote in before the replay response arrived, and `remoteId` is unique. Returns false when the local
     * row is gone, so the caller can undo the remote insert.
     */
    @Transaction
    suspend fun markSynced(
        localId: Long,
        remoteId: Long,
    ): Boolean {
        deleteSyncedWithRemoteId(
            remoteId = remoteId,
            exceptLocalId = localId,
        )

        return setSynced(
            localId = localId,
            remoteId = remoteId,
        ) > 0
    }

    /**
     * Swaps the book's synced rows for [remote]. A remote row held by an unsynced local row is skipped
     * (a pending delete must not resurrect); one matching a pending insert is that insert whose response
     * was lost, so the local row adopts the remote id instead of being duplicated.
     */
    @Transaction
    suspend fun replaceSyncedForBook(
        bookId: Int,
        remote: List<QuoteEntity>,
    ) {
        deleteSyncedForBook(bookId = bookId)

        val pendingInserts: List<QuoteEntity> = getPendingInsertsForBook(bookId = bookId)

        remote.forEach { row ->
            val remoteId: Long = row.remoteId ?: return@forEach

            if (hasUnsyncedRowWithRemoteId(remoteId = remoteId)) return@forEach

            val pendingMatch: QuoteEntity? = pendingInserts.firstOrNull { it.isSameQuoteAs(other = row) }

            if (pendingMatch == null) {
                insertSynced(entity = row)
            } else {
                setSynced(
                    localId = pendingMatch.localId,
                    remoteId = remoteId,
                )
                deletePendingInsertFor(quoteLocalId = pendingMatch.localId)
            }
        }
    }
}
