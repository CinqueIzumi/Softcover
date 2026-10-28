package nl.rhaydus.softcover.core.personal.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.dao.ProgressHistoryDao
import nl.rhaydus.softcover.core.database.model.ProgressHistoryEntity

interface ReadingJournalHistoryLocalDataSource {
    fun observeByBookId(bookId: Int): Flow<List<ProgressHistoryEntity>>

    suspend fun replaceSyncedForBook(
        bookId: Int,
        entities: List<ProgressHistoryEntity>,
    )

    suspend fun replaceAllSynced(entities: List<ProgressHistoryEntity>)
}

internal class ReadingJournalHistoryLocalDataSourceImpl(
    private val dao: ProgressHistoryDao,
) : ReadingJournalHistoryLocalDataSource {
    override fun observeByBookId(bookId: Int): Flow<List<ProgressHistoryEntity>> =
        dao.observeByBookId(bookId = bookId)

    override suspend fun replaceSyncedForBook(
        bookId: Int,
        entities: List<ProgressHistoryEntity>,
    ) = dao.replaceSyncedForBook(
        bookId = bookId,
        entities = entities,
    )

    override suspend fun replaceAllSynced(entities: List<ProgressHistoryEntity>) =
        dao.replaceAllSynced(entities = entities)
}
