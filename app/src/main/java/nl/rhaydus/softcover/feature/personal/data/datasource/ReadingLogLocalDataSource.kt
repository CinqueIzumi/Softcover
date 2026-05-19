package nl.rhaydus.softcover.feature.personal.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.personal.data.dao.ReadingLogDao
import nl.rhaydus.softcover.feature.personal.data.model.ReadingLogEntryEntity

interface ReadingLogLocalDataSource {
    fun observeByBookId(bookId: Int): Flow<List<ReadingLogEntryEntity>>

    fun observeCountByBookId(bookId: Int): Flow<Int>

    suspend fun insert(entity: ReadingLogEntryEntity): Long

    suspend fun update(entity: ReadingLogEntryEntity)

    suspend fun delete(id: Long)
}

class ReadingLogLocalDataSourceImpl(
    private val dao: ReadingLogDao,
) : ReadingLogLocalDataSource {
    override fun observeByBookId(bookId: Int): Flow<List<ReadingLogEntryEntity>> =
        dao.observeByBookId(bookId = bookId)

    override fun observeCountByBookId(bookId: Int): Flow<Int> =
        dao.observeCountByBookId(bookId = bookId)

    override suspend fun insert(entity: ReadingLogEntryEntity): Long = dao.insert(entity = entity)

    override suspend fun update(entity: ReadingLogEntryEntity) = dao.update(entity = entity)

    override suspend fun delete(id: Long) = dao.deleteById(id = id)
}
