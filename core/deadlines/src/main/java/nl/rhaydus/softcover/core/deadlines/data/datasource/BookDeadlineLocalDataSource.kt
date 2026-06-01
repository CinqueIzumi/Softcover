package nl.rhaydus.softcover.core.deadlines.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.data.database.dao.BookDeadlineDao
import nl.rhaydus.softcover.core.data.database.model.BookDeadlineEntity

interface BookDeadlineLocalDataSource {
    fun observe(bookId: Int): Flow<BookDeadlineEntity?>

    fun observeAll(): Flow<List<BookDeadlineEntity>>

    suspend fun upsert(entity: BookDeadlineEntity)

    suspend fun delete(bookId: Int)
}

class BookDeadlineLocalDataSourceImpl(
    private val dao: BookDeadlineDao,
) : BookDeadlineLocalDataSource {
    override fun observe(bookId: Int): Flow<BookDeadlineEntity?> =
        dao.observeByBookId(bookId = bookId)

    override fun observeAll(): Flow<List<BookDeadlineEntity>> = dao.observeAll()

    override suspend fun upsert(entity: BookDeadlineEntity) = dao.upsert(entity = entity)

    override suspend fun delete(bookId: Int) = dao.deleteByBookId(bookId = bookId)
}
