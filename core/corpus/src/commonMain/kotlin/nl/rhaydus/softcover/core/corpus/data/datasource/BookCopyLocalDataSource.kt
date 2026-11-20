package nl.rhaydus.softcover.core.corpus.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.dao.BookCopyDao
import nl.rhaydus.softcover.core.database.model.BookCopyEntity

internal interface BookCopyLocalDataSource {
    fun observeAll(): Flow<List<BookCopyEntity>>

    fun observe(bookId: Int): Flow<BookCopyEntity?>

    suspend fun get(bookId: Int): BookCopyEntity?

    suspend fun upsert(entity: BookCopyEntity)

    suspend fun delete(bookId: Int)
}

internal class BookCopyLocalDataSourceImpl(
    private val dao: BookCopyDao,
) : BookCopyLocalDataSource {
    override fun observeAll(): Flow<List<BookCopyEntity>> = dao.observeAll()

    override fun observe(bookId: Int): Flow<BookCopyEntity?> = dao.observe(bookId = bookId)

    override suspend fun get(bookId: Int): BookCopyEntity? = dao.get(bookId = bookId)

    override suspend fun upsert(entity: BookCopyEntity) = dao.upsert(entity = entity)

    override suspend fun delete(bookId: Int) = dao.delete(bookId = bookId)
}
