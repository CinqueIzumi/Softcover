package nl.rhaydus.softcover.core.corpus.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.dao.BookTraitsDao
import nl.rhaydus.softcover.core.database.model.BookTraitsEntity

internal interface BookTraitsLocalDataSource {
    fun observe(bookId: Int): Flow<BookTraitsEntity?>

    suspend fun get(bookId: Int): BookTraitsEntity?

    suspend fun upsert(entity: BookTraitsEntity)

    suspend fun delete(bookId: Int)
}

internal class BookTraitsLocalDataSourceImpl(
    private val dao: BookTraitsDao,
) : BookTraitsLocalDataSource {
    override fun observe(bookId: Int): Flow<BookTraitsEntity?> = dao.observe(bookId = bookId)

    override suspend fun get(bookId: Int): BookTraitsEntity? = dao.get(bookId = bookId)

    override suspend fun upsert(entity: BookTraitsEntity) = dao.upsert(entity = entity)

    override suspend fun delete(bookId: Int) = dao.delete(bookId = bookId)
}
