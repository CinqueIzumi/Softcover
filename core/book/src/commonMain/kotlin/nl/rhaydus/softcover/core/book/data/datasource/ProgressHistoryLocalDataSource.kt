package nl.rhaydus.softcover.core.book.data.datasource

import nl.rhaydus.softcover.core.database.dao.ProgressHistoryDao
import nl.rhaydus.softcover.core.database.model.ProgressHistoryEntity

interface ProgressHistoryLocalDataSource {
    suspend fun insertPending(
        bookId: Int,
        actionAtEpochMillis: Long,
        pages: Int?,
        seconds: Int?,
    ): Long

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
    ): Long = dao.insert(
        entity = ProgressHistoryEntity(
            bookId = bookId,
            actionAtEpochMillis = actionAtEpochMillis,
            pages = pages,
            seconds = seconds,
            pending = true,
        ),
    )

    override suspend fun markSynced(localId: Long) = dao.markSynced(localId = localId)

    override suspend fun delete(localId: Long) = dao.deleteById(localId = localId)
}
