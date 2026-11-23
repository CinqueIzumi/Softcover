package nl.rhaydus.softcover.core.corpus.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.dao.MoodMomentsDao
import nl.rhaydus.softcover.core.database.model.MoodMomentEntity

internal interface MoodMomentsLocalDataSource {
    fun observeAll(): Flow<List<MoodMomentEntity>>

    fun observe(bookId: Int): Flow<List<MoodMomentEntity>>

    suspend fun insert(entity: MoodMomentEntity): Long

    suspend fun update(entity: MoodMomentEntity)

    suspend fun delete(id: Long)
}

internal class MoodMomentsLocalDataSourceImpl(
    private val dao: MoodMomentsDao,
) : MoodMomentsLocalDataSource {
    override fun observeAll(): Flow<List<MoodMomentEntity>> = dao.observeAll()

    override fun observe(bookId: Int): Flow<List<MoodMomentEntity>> = dao.observe(bookId = bookId)

    override suspend fun insert(entity: MoodMomentEntity): Long = dao.insert(entity = entity)

    override suspend fun update(entity: MoodMomentEntity) = dao.update(entity = entity)

    override suspend fun delete(id: Long) = dao.delete(id = id)
}
