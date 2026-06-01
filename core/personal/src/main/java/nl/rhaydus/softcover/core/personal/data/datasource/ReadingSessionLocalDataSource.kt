package nl.rhaydus.softcover.core.personal.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.data.database.dao.ReadingSessionDao
import nl.rhaydus.softcover.core.data.database.model.ReadingSessionEntity

interface ReadingSessionLocalDataSource {
    fun observeByBookId(bookId: Int): Flow<List<ReadingSessionEntity>>

    fun observeAll(): Flow<List<ReadingSessionEntity>>

    fun observeActive(): Flow<ReadingSessionEntity?>

    fun observeById(id: Long): Flow<ReadingSessionEntity?>

    suspend fun insert(entity: ReadingSessionEntity): Long

    suspend fun update(entity: ReadingSessionEntity)

    suspend fun delete(id: Long)
}

class ReadingSessionLocalDataSourceImpl(
    private val dao: ReadingSessionDao,
) : ReadingSessionLocalDataSource {
    override fun observeByBookId(bookId: Int): Flow<List<ReadingSessionEntity>> =
        dao.observeByBookId(bookId = bookId)

    override fun observeAll(): Flow<List<ReadingSessionEntity>> = dao.observeAll()

    override fun observeActive(): Flow<ReadingSessionEntity?> = dao.observeActive()

    override fun observeById(id: Long): Flow<ReadingSessionEntity?> = dao.observeById(id = id)

    override suspend fun insert(entity: ReadingSessionEntity): Long = dao.insert(entity = entity)

    override suspend fun update(entity: ReadingSessionEntity) = dao.update(entity = entity)

    override suspend fun delete(id: Long) = dao.deleteById(id = id)
}
