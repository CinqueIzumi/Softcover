package nl.rhaydus.softcover.core.book.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.dao.ProgressHistoryDao
import nl.rhaydus.softcover.core.database.model.ProgressHistoryEntity
import nl.rhaydus.softcover.core.database.model.ProgressUpdateRow

interface ProgressHistoryLocalDataSource {
    suspend fun insertPending(
        bookId: Int,
        actionAtEpochMillis: Long,
        pages: Int?,
        seconds: Int?,
        pagesRead: Int,
    ): Long

    fun observeAllUpdates(): Flow<List<ProgressUpdateRow>>

    suspend fun markSynced(localId: Long)

    suspend fun delete(localId: Long)
}

internal class ProgressHistoryLocalDataSourceImpl(
    private val dao: ProgressHistoryDao,
) : ProgressHistoryLocalDataSource {
    override suspend fun insertPending(
        bookId: Int,
        actionAtEpochMillis: Long,
        pages: Int?,
        seconds: Int?,
        pagesRead: Int,
    ): Long = dao.insert(
        entity = ProgressHistoryEntity(
            bookId = bookId,
            actionAtEpochMillis = actionAtEpochMillis,
            pages = pages,
            seconds = seconds,
            pending = true,
            pagesRead = pagesRead,
        ),
    )

    override fun observeAllUpdates(): Flow<List<ProgressUpdateRow>> = dao.observeAllUpdates()

    override suspend fun markSynced(localId: Long) = dao.markSynced(localId = localId)

    override suspend fun delete(localId: Long) = dao.deleteById(localId = localId)
}
