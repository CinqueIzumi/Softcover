package nl.rhaydus.softcover.core.personal.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.dao.ProgressHistoryDao
import nl.rhaydus.softcover.core.database.model.ProgressHistoryEntity

interface ReadingJournalHistoryLocalDataSource {
    fun observeByBookId(bookId: Int): Flow<List<ProgressHistoryEntity>>

    suspend fun getSyncedIds(bookId: Int): List<Long>

    suspend fun replaceSynced(
        syncedIdsBeforeFetch: List<Long>,
        entities: List<ProgressHistoryEntity>,
    )
}

internal class ReadingJournalHistoryLocalDataSourceImpl(
    private val dao: ProgressHistoryDao,
) : ReadingJournalHistoryLocalDataSource {
    override fun observeByBookId(bookId: Int): Flow<List<ProgressHistoryEntity>> =
        dao.observeByBookId(bookId = bookId)

    override suspend fun getSyncedIds(bookId: Int): List<Long> = dao.getSyncedIdsForBook(bookId = bookId)

    override suspend fun replaceSynced(
        syncedIdsBeforeFetch: List<Long>,
        entities: List<ProgressHistoryEntity>,
    ) = dao.replaceSynced(
        syncedIdsBeforeFetch = syncedIdsBeforeFetch,
        entities = entities,
    )
}
