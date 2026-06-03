package nl.rhaydus.softcover.core.personal.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.dao.HighlightDao
import nl.rhaydus.softcover.core.database.model.HighlightEntity

interface HighlightLocalDataSource {
    fun observeByBookId(bookId: Int): Flow<List<HighlightEntity>>

    fun observeAll(): Flow<List<HighlightEntity>>

    fun observeById(id: Long): Flow<HighlightEntity?>

    suspend fun insert(entity: HighlightEntity): Long

    suspend fun update(entity: HighlightEntity)

    suspend fun delete(id: Long)
}

internal class HighlightLocalDataSourceImpl(
    private val dao: HighlightDao,
) : HighlightLocalDataSource {
    override fun observeByBookId(bookId: Int): Flow<List<HighlightEntity>> =
        dao.observeByBookId(bookId = bookId)

    override fun observeAll(): Flow<List<HighlightEntity>> = dao.observeAll()

    override fun observeById(id: Long): Flow<HighlightEntity?> = dao.observeById(id = id)

    override suspend fun insert(entity: HighlightEntity): Long = dao.insert(entity = entity)

    override suspend fun update(entity: HighlightEntity) = dao.update(entity = entity)

    override suspend fun delete(id: Long) = dao.deleteById(id = id)
}
