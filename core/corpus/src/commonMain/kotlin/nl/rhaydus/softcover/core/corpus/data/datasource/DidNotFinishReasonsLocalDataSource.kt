package nl.rhaydus.softcover.core.corpus.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.dao.DidNotFinishReasonsDao
import nl.rhaydus.softcover.core.database.model.DidNotFinishReasonsEntity

internal interface DidNotFinishReasonsLocalDataSource {
    fun observeAll(): Flow<List<DidNotFinishReasonsEntity>>

    fun observe(bookId: Int): Flow<DidNotFinishReasonsEntity?>

    suspend fun get(bookId: Int): DidNotFinishReasonsEntity?

    suspend fun upsert(entity: DidNotFinishReasonsEntity)

    suspend fun delete(bookId: Int)
}

internal class DidNotFinishReasonsLocalDataSourceImpl(
    private val dao: DidNotFinishReasonsDao,
) : DidNotFinishReasonsLocalDataSource {
    override fun observeAll(): Flow<List<DidNotFinishReasonsEntity>> = dao.observeAll()

    override fun observe(bookId: Int): Flow<DidNotFinishReasonsEntity?> = dao.observe(bookId = bookId)

    override suspend fun get(bookId: Int): DidNotFinishReasonsEntity? = dao.get(bookId = bookId)

    override suspend fun upsert(entity: DidNotFinishReasonsEntity) = dao.upsert(entity = entity)

    override suspend fun delete(bookId: Int) = dao.delete(bookId = bookId)
}
